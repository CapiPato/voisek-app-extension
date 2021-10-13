/*
 * @providesModule react-native-call-detection
 */
import { NativeModules, Platform } from 'react-native';

const { VoisekCallStateExtensionAndroid } = NativeModules;
const BatchedBridge = require('react-native/Libraries/BatchedBridge/BatchedBridge');

var VoisekCallStateUpdateActionModule = require('./VoisekCallStateUpdateActionModule');
BatchedBridge.registerCallableModule(
  'VoisekCallStateUpdateActionModule',
  VoisekCallStateUpdateActionModule
);

class VoisekCallStateManager {
  subscription: any;
  callback: any;
  constructor(callback: any) {
    this.callback = callback;
    if (Platform.OS !== 'ios') {
      if (VoisekCallStateExtensionAndroid) {
        VoisekCallStateExtensionAndroid.startCallerListener();
      }
      VoisekCallStateUpdateActionModule.callback = this.callback;
    }
  }

  dispose() {
    VoisekCallStateExtensionAndroid &&
      VoisekCallStateExtensionAndroid.stopCallerListener();
    VoisekCallStateUpdateActionModule.callback = undefined;
    if (this.subscription) {
      this.subscription.removeAllListeners('PhoneCallStateUpdate');
      this.subscription = undefined;
    }
  }
}
export default VoisekCallStateManager;
