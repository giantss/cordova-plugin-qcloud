# Cordova_QCloud_插件

	
这个一个QCloud SDK的Cordova 插件。 		

##主要功能
- 视频上传腾讯云
	

##安装要求
- Cordova Version >=3.5
- Cordova-Android >=4.0
- Cordova-iOS >=4.0

##安装
1. 命令行运行      ```cordova plugin add https://github.com/giantss/cordova-plugin-qcloud --variable QCLOUD_APP_ID=YOUR_QCLOUD_APP_ID```  或者  ```cordova plugin add cordova-plugin-qcloud --variable QCLOUD_APP_ID=YOUR_QCLOUD_APP_ID```              
2. 命令行运行 cordova build --device     
 		
##注意事项					        	
1. 这个插件要求cordova-android 的版本 >=4.0,推荐使用 cordova  5.0.0 或更高的版本，因为从cordova 5.0 开始cordova-android 4.0 是默认使用的android版本
2.  请在cordova的deviceready事件触发以后再调用本插件！！！		
3. <del>在低于5.1.1的cordova版本中存在一个Bug，如果你有多个插件要修改iOS工程中的 “*-Info.plist” CFBundleURLTypes, 只有第一个安装的插件才会生效.所以安装完插件请务必在你的Xcode工程里面检查一下URLTypes。 关于这个bug的详情你可以在 [这里](https://issues.apache.org/jira/browse/CB-8007)找到</del> 建议安装使用5.1.1及以上的cordova版本 	
4. Android版本请确保你的签名是正确的			
				

##使用方式                								
					     


