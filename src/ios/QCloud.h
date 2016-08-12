#import <Cordova/CDVPlugin.h>
#import "TXYUploadManager.h"
#import "M13ProgressViewRing.h"
#import "M13ProgressHUD.h"


@interface QCloud : CDVPlugin

@property (nonatomic,strong) NSMutableData *dataSign;
@property (nonatomic,strong) NSMutableData *qcloudCBData;
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
@property(nonatomic, copy) NSString *relativeSDPath;
@property(nonatomic, copy) NSString *absoluteSDPath;
@property(nonatomic, copy) NSString *fixedURL;
@property(nonatomic, copy) NSString *CBURL;
@property (nonatomic, copy) NSString *reqType;
@property (nonatomic, copy) NSString *playUrl;
@property (nonatomic, copy) NSString *coverUrl;
@property (nonatomic, copy) NSString *isEncodeSuccess;
@property (nonatomic,strong) M13ProgressHUD *HUD;

@property (nonatomic) SEL callBack;




@property NSString *ok;


- (void) upLoadVideo: (CDVInvokedUrlCommand *)command;

- (void) getSignWithUrl:(CDVInvokedUrlCommand *)command;

- (void) getEncodeState: (NSString *)s;

@end