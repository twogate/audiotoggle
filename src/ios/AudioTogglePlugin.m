#import "AudioTogglePlugin.h"
#import <AudioToolbox/AudioToolbox.h>
#import <AVFoundation/AVFoundation.h>

@implementation AudioTogglePlugin

- (void)setAudioMode:(CDVInvokedUrlCommand *)command
{
  NSError* __autoreleasing err = nil;
  NSString* mode = [NSString stringWithFormat:@"%@", [command.arguments objectAtIndex:0]];

  AVAudioSession *session = [AVAudioSession sharedInstance];

  if ([mode isEqualToString:@"earpiece"]) {
    [session setCategory:AVAudioSessionCategoryPlayAndRecord error:&err];
    [session overrideOutputAudioPort:kAudioSessionProperty_OverrideAudioRoute error:&err];
  } else if ([mode isEqualToString:@"speaker"]) {
    [session setCategory:AVAudioSessionCategoryPlayAndRecord withOptions:AVAudioSessionCategoryOptionDefaultToSpeaker error:&err];
  } else if ([mode isEqualToString:@"normal"]) {
    [session setCategory:AVAudioSessionCategorySoloAmbient error:&err];
  }
}

@end
