//
// Created by Anatolii Chub on 04/01/2026.
//


#import <Foundation/Foundation.h>

@interface LiveActivityBridgeImpl : NSObject
+ (NSString * _Nullable)startActivityWithActivityName:(NSString * _Nonnull)activityName
        heartRate:(NSInteger)heartRate
        isConnected:(BOOL)isConnected
        isContactOn:(BOOL)isContactOn
        bleState:(NSString * _Nonnull)bleState
        isTrackingActive:(BOOL)isTrackingActive
        batteryLevel:(NSInteger)batteryLevel
        deviceName:(NSString * _Nonnull)deviceName
        elapsedTime:(int64_t)elapsedTime;
+ (void)updateActivityWithActivityId:(NSString * _Nonnull)activityId
        heartRate:(NSInteger)heartRate
        isConnected:(BOOL)isConnected
        isContactOn:(BOOL)isContactOn
        bleState:(NSString * _Nonnull)bleState
        isTrackingActive:(BOOL)isTrackingActive
        batteryLevel:(NSInteger)batteryLevel
        deviceName:(NSString * _Nonnull)deviceName
        elapsedTime:(int64_t)elapsedTime;
+ (void)endActivityWithActivityId:(NSString * _Nonnull)activityId;
@end
