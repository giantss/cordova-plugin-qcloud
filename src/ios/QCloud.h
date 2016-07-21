#import <Cordova/CDVPlugin.h>
#import "TXYUploadManager.h"
#import "MBProgressHUD.h"

@interface QCloud : CDVPlugin

@property (nonatomic,strong) NSMutableData *dataSign;
@property (nonatomic,copy) NSString *sign;
@property (nonatomic,strong) TXYUploadManager *uploadVideoManager;
@property (nonatomic,strong) TXYVideoUploadTaskRsp *photoResp;
@property (nonatomic,strong) TXYVideoUploadTask *uploadVideoTask;
@property(nonatomic, copy) NSString *signUrl;
@property(nonatomic, copy) NSString *videoPath;
@property (nonatomic,copy) NSString *oneSign;
@property(nonatomic, copy) NSString *callback;
@property(nonatomic, copy) NSString *fileName;
@property(nonatomic, copy) NSString *bucket;
@property(nonatomic, copy) NSString *appId;
@property(nonatomic, copy) NSString *persistenceId;
@property (nonatomic,strong) MBProgressHUD *loadingView;
@property (nonatomic) SEL callBack;




@property NSString *ok;


- (void) upLoadVideo: (CDVInvokedUrlCommand *)command;

- (void) getSignWithUrl: (NSString *)s callBack:(SEL)finish;

@end