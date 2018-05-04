#import "AudioTogglePlugin.h"
#import <AudioToolbox/AudioToolbox.h>
#import <AVFoundation/AVFoundation.h>

@implementation AudioTogglePlugin
{
    NSString *mode;
}
- (void)setAutoRoute:(CDVInvokedUrlCommand *)command
{
    mode = [NSString stringWithFormat:@"%@", [command.arguments objectAtIndex:0]];
    
    [self configureAVAudioSession];
}

- (void)onAppTerminate
{
    [self unconfigureAVAudioSession];
}

- (void)configureAVAudioSession {
    [self changeRoute];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didSessionRouteChange:) name:AVAudioSessionRouteChangeNotification object:nil];
}

- (void)unconfigureAVAudioSession {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:AVAudioSessionRouteChangeNotification object:nil];
}

- (void)changeRoute {
    NSError* error;

    AVAudioSession *session = [AVAudioSession sharedInstance];

    if ([self isHeadsetPluggedIn]) {
        [session setCategory:AVAudioSessionCategoryPlayback error:&error];
        [session overrideOutputAudioPort: AVAudioSessionPortOverrideNone error:&error];
    } else {
        [session setCategory:AVAudioSessionCategoryPlayAndRecord
                 withOptions:AVAudioSessionCategoryOptionAllowBluetoothA2DP
                 error:&error];
        [session overrideOutputAudioPort:kAudioSessionProperty_OverrideAudioRoute error:&error];
    }
}

- (void)didSessionRouteChange:(NSNotification *)notification {
    NSDictionary *interuptionDict = notification.userInfo;
    NSInteger routeChangeReason = [[interuptionDict valueForKey:AVAudioSessionRouteChangeReasonKey] integerValue];
    NSLog(@"AVAudioSessionRouteChangeReasonKey: %zd", routeChangeReason);

    switch (routeChangeReason) {
        // 取り外し
        case AVAudioSessionRouteChangeReasonOldDeviceUnavailable: {
            [self changeRoute];
        }
            break;
            
        // 新規接続
        case AVAudioSessionRouteChangeReasonNewDeviceAvailable: {
            [self changeRoute];
        }
            
        default:
            break;
    }
}

- (BOOL)isHeadsetPluggedIn {
    // headphone と定義する output
    NSSet *headphoneStrings = [NSSet setWithObjects:AVAudioSessionPortHeadphones,
                             AVAudioSessionPortBluetoothA2DP,
                             nil];
    
    AVAudioSessionRouteDescription* route = [[AVAudioSession sharedInstance] currentRoute];
    for (AVAudioSessionPortDescription* desc in [route outputs]) {
        if ([headphoneStrings containsObject:[desc portType]]) {
            return YES;
        }
    }
    return NO;
}

@end
