#import "QCloud.h"
#define DECLARE_WEAK_SELF __typeof(&*self) __weak weakSelf = self
#define DECLARE_STRONG_SELF __typeof(&*self) __strong strongSelf = weakSelf

NSString *QCLOUD_APP_ID = @"qcloud_app_id";
NSString *QCLOUD_VIDEO_UPLOAD_FAILDED = @"视频上传失败";
NSString *QCLOUD_VIDEO_UPLOAD_SUCCESS = @"视频上传成功";
NSString *QCLOUD_METHED_SUCCESS = @"方法调用失败";




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
    _fixedURL = @"http://cbiphone-10047633.video.myqcloud.com";
    _CBURL = @"http://bbs.chinabike.net/phone/video_encode.php?action=video_info&playurl=";
    }


/**
  *  视频上传
 */
- (void)upLoadVideo:(CDVInvokedUrlCommand *)command
{
    
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
                
                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:QCLOUD_METHED_SUCCESS];
               [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        
    }
    
    _uploadVideoManager = [[TXYUploadManager alloc] initWithCloudType:TXYCloudTypeForVideo
                                                        persistenceId:_persistenceId
                                                                appId:_appId];
    
    [self getSignWithUrl:command];
}


-(void)getSignWithUrl:(CDVInvokedUrlCommand *)command
{
  
    _dataSign = [NSMutableData new];
    NSURL *url =  [NSURL URLWithString:_signUrl];//请求的地址请更换成自己业务服务器的地址
    NSURLRequest *request = [[NSURLRequest alloc]initWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:10];
//    NSURLConnection *connection = [[NSURLConnection alloc]initWithRequest:request delegate:self];
//    [connection start];
    
    NSOperationQueue *queue=[NSOperationQueue mainQueue];
    [NSURLConnection sendAsynchronousRequest:request queue:queue completionHandler:^(NSURLResponse *response, NSData *data, NSError *connectionError) {
        if (connectionError) {
            NSLog(@"网络连接出错:%@", connectionError.userInfo[@"NSLocalizedDescription"]);
            return ;
        }
        
       [self.dataSign appendData:data];
        // 解析成字符串数据
        self.sign= nil;
        NSDictionary *responseDic = [NSJSONSerialization JSONObjectWithData:_dataSign options:kNilOptions error:nil];
        _sign = [responseDic objectForKey:@"sign"];
        if(self.sign){
            TXYVideoFileInfo *videoFileInfo = [[TXYVideoFileInfo alloc]init];
            videoFileInfo.title = @"video2";
            videoFileInfo.desc = @"videodesc1";
            videoFileInfo.coverUrl= @"http://www.chinabike.net/uploadfile/2016/0719/20160719063757571.jpg";//视频的自定义封面
            
            
            
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
                                   //                               NSLog(@"上传视频的url%@ 上传视频的fileid = %@",_photoResp.fileURL,_photoResp.fileId);
                                   //                               NSLog(@"video is source url%@",_photoResp.sourceURL);
                                   //[self setImageMessage:_photoResp.fileURL];
                                   // NSLog(@"upload return=%d",_photoResp.retCode);
                                   
                                   
                                   if(_photoResp.retCode >= 0){
                                       
//                                       CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:QCLOUD_VIDEO_UPLOAD_SUCCESS];
//                                       [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                                       //上传成功
                                       NSLog(@"视频上传成功");
                                       
                                       //处理地址
                                       NSRange range =[_photoResp.fileURL rangeOfString:@"/ios"];
                                       _relativeSDPath = [@"/10047633/cbiphone" stringByAppendingString:[[_photoResp.fileURL substringFromIndex:range.location] stringByAppendingString:@".f20.mp4"]];
                                       
                                       _CBURL = [_CBURL stringByAppendingString:_relativeSDPath];
                                       [self getEncodeState:_CBURL];
                                       
                                   }else{
                                       //上传失败
                                       NSLog(@"视频上传失败");
                                         CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:QCLOUD_VIDEO_UPLOAD_FAILDED];
                                        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                                       
                                   }
                                   
                                   
                               }
                               progress:^(int64_t totalSize, int64_t sendSize, NSDictionary *context) {
                                   
                                   //上传进度
                                   //命中妙传，不走这里的！
                                   NSLog(@" totalSize %lld",totalSize);
                                   NSLog(@" sendSize %lld",sendSize);
                                   //NSLog(@" sendSize %@",context);
                               }
                            stateChange:^(TXYUploadTaskState state, NSDictionary *context) {
                                //上传状态变化
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
[self getSignWithUrl:command];
        
        }
        
        
    }];
    
    
    
    
    
    
    
    
}

-(void) getEncodeState:(NSString *)s
{
    _reqType = @"encode";
    _qcloudCBData = [NSMutableData new];
    NSURL *url =  [NSURL URLWithString:s ];
    NSURLRequest *request = [[NSURLRequest alloc]initWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:10];
    NSOperationQueue *queue=[NSOperationQueue mainQueue];
    [NSURLConnection sendAsynchronousRequest:request queue:queue completionHandler:^(NSURLResponse *response, NSData *data, NSError *connectionError) {
        if (connectionError) {
            NSLog(@"网络连接出错:%@", connectionError.userInfo[@"NSLocalizedDescription"]);
            return ;
        }
        
        _isEncodeSuccess = nil;
        
            [self.qcloudCBData appendData:data];
        
        NSDictionary *respEncodeDic = [NSJSONSerialization JSONObjectWithData:_qcloudCBData options:kNilOptions error:nil];
        _isEncodeSuccess = [respEncodeDic objectForKey:@"success"];
        if([self.isEncodeSuccess  isEqual: @"1"]){
            NSLog(@"转码完毕");
            _playUrl = [respEncodeDic objectForKey:@"playurl"];
            _coverUrl = [respEncodeDic objectForKey:@"cover_url"];
            //处理地址
            NSRange range =[_playUrl rangeOfString:@"/ios"];
            _playUrl = [_fixedURL stringByAppendingString:[_playUrl substringFromIndex:range.location]];
            
         
            
            NSMutableDictionary *Dic = [NSMutableDictionary dictionaryWithCapacity:2];
            [Dic setObject:_playUrl forKey:@"absoluteSDPath"];
            [Dic setObject:_coverUrl forKey:@"cbCoverUrl"];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:Dic];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            
            
        }else{
            NSLog(@"转码中");
            
            [self performSelector:@selector(getEncodeState:) withObject:_CBURL afterDelay:2];

        }
        
        }];
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