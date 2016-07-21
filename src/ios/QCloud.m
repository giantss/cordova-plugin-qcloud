#import "QCloud.h"
#define DECLARE_WEAK_SELF __typeof(&*self) __weak weakSelf = self
#define DECLARE_STRONG_SELF __typeof(&*self) __strong strongSelf = weakSelf



NSString *QCLOUD_APP_ID = @"qcloud_app_id";

@implementation QCloud
/**
 *  插件初始化主要用于appkey的注册
 */
- (void)pluginInitialize {
    NSString *appId = [[self.commandDelegate settings] objectForKey:QCLOUD_APP_ID];
    _appId = appId;
    _bucket = @"cbiphone";
    _persistenceId = @"chinabikeqcloudvideo";
    _signUrl = @"http://bbs.chinabike.net/phone/uploadvideo.php";
    _oneSign = @"http://bbs.chinabike.net/phone/uploadvideo_once.php";
    }/**
  *  视频上传
 */
- (void)upLoadVideo:(CDVInvokedUrlCommand *)command
{
    [self getUploadImageSign];

     _callback = command.callbackId;
    // 取得js传递过来的参数
    NSDictionary* path = [command argumentAtIndex:0];
    if(path){
        _videoPath = [path objectForKey:@"path"];
        //_videoPath = [[path objectForKey:@"path"] substringFromIndex:8];
        
        UInt64 recordTime = [[NSDate date] timeIntervalSince1970]*1000;
        
        NSDateFormatter * formatter = [[NSDateFormatter alloc ] init];
        [formatter setDateFormat:@"YYYYMMdd_hhmmssSSS"];
        NSString *date =  [formatter stringFromDate:[NSDate date]];
        NSString *timeLocal = [[NSString alloc] initWithFormat:@"%@", date];
        NSLog(@"%@", timeLocal);
            _fileName = [@"ios/" stringByAppendingString:[timeLocal stringByAppendingString:@".mov"] ];
        
            }else{
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        
    }
    
    _uploadVideoManager = [[TXYUploadManager alloc] initWithCloudType:TXYCloudTypeForVideo
                                                        persistenceId:_persistenceId
                                                                appId:_appId];
    
}

-(void)getUploadImageSign
{
    [self getSignWithUrl:_signUrl callBack:@selector(getSignFinis:)];
   
}

#pragma mark －network

-(void)getSignFinis:(NSString *)string
{
    _sign = string;
}
-(void)getSignWithUrl:(NSString *)s callBack:(SEL)finish
{
    _dataSign = [NSMutableData new];
    _callBack = finish;
    NSURL *url =  [NSURL URLWithString:s ];//请求的地址请更换成自己业务服务器的地址
    NSURLRequest *request = [[NSURLRequest alloc]initWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:10];
    NSURLConnection *connection = [[NSURLConnection alloc]initWithRequest:request delegate:self];
    [connection start];
}
#pragma mark - NSURLConnectionDataDelegate
#pragma mark 接收到服务器返回的数据时调用（如果数据比较多，这个方法可能会被调用多次）

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
    [self.dataSign appendData:data];
}

#pragma mark 网络连接出错时调用
- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error {
    NSLog(@"网络连接出错:%@", [error localizedDescription]);
}

#pragma mark 服务器的数据已经接收完毕时调用
- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
    // 解析成字符串数据
    self.sign= nil;
    NSDictionary *responseDic = [NSJSONSerialization JSONObjectWithData:_dataSign options:kNilOptions error:nil];
    _sign = [responseDic objectForKey:@"sign"] ;
    
    if(self.sign){
        TXYVideoFileInfo *videoFileInfo = [[TXYVideoFileInfo alloc]init];
        videoFileInfo.title = @"video2";
        videoFileInfo.desc = @"videodesc1";
        videoFileInfo.coverUrl= @"http://baron-10000002.cos.myqcloud.com/me/2006420115622257.jpg";//视频的自定义封面
        
        
        
        //上传视频总共四步之    第四步: 初始化上传化上传任务
        NSString *dir =[NSString stringWithFormat:@"/"];//[NSString stringWithFormat:@"/12",bucket];
        self.uploadVideoTask = [[TXYVideoUploadTask alloc] initWithPath:_videoPath
                                                                   sign:_sign
                                                                 bucket:_bucket
                                                               fileName:_fileName
                                                        customAttribute:@"customAttribute"
                                                        uploadDirectory:dir
                                                          videoFileInfo:videoFileInfo
                                                             msgContext:@"msgContext"
                                                             insertOnly:NO];
        
        
        
        [self showLoadingWithView:self.webView];
        //上传视频总共五步之   第五步: 上传任务
        DECLARE_WEAK_SELF;
        [_uploadVideoManager upload:_uploadVideoTask
                           complete:^(TXYTaskRsp *resp, NSDictionary *context) {
                               DECLARE_STRONG_SELF;
                               if (!strongSelf) return;
                               [self hiddenLoadingWihtView:self.webView];
                               _photoResp = (TXYVideoUploadTaskRsp *)resp;
                               NSLog(@"上传视频的url%@ 上传视频的fileid = %@",_photoResp.fileURL,_photoResp.fileId);
                               NSLog(@"video is source url%@",_photoResp.sourceURL);
                               
                               
                               //[self setImageMessage:_photoResp.fileURL];
                               NSLog(@"upload return=%d",_photoResp.retCode);
                           }
                           progress:^(int64_t totalSize, int64_t sendSize, NSDictionary *context) {
                               //命中妙传，不走这里的！
                               //                           NSLog(@" totalSize %lld",totalSize);
                               //                           NSLog(@" sendSize %lld",sendSize);
                               //                           NSLog(@" sendSize %@",context);
                           }
                        stateChange:^(TXYUploadTaskState state, NSDictionary *context) {
                            switch (state) {
                                case TXYUploadTaskStateWait:
                                    NSLog(@"demoapp log 任务等待中");
                                    break;
                                case TXYUploadTaskStateConnecting:
                                    NSLog(@"demoapp log 任务连接中");
                                    break;
                                case TXYUploadTaskStateFail:
                                    NSLog(@"demoapp log 任务失败");
                                    break;
                                case TXYUploadTaskStateSuccess:
                                    NSLog(@"demoapp log 任务成功");
                                    break;
                                default:
                                    break;
                            }}];

            }else{
                    [self getUploadImageSign];
    }

}

-(void)showLoadingWithView:(UIView *)v
{
    
    //_loadingView = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    _loadingView = [MBProgressHUD showHUDAddedTo:self.webView animated:YES];
    _loadingView.mode = MBProgressHUDModeIndeterminate;
    
    //    _loadingView.margin = 10.f;
    //    _loadingView.yOffset = 150.f;
    _loadingView.removeFromSuperViewOnHide = YES;
    [_loadingView show:YES];
}
-(void)hiddenLoadingWihtView:(UIView *)v
{
    [MBProgressHUD hideAllHUDsForView:v animated:YES];
}
@end