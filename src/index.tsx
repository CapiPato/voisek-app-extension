import { NativeModules } from 'react-native';

type VoisekAppExtensionType = {
  multiply(a: number, b: number): Promise<number>;
};

const { VoisekAppExtension } = NativeModules;

export default VoisekAppExtension as VoisekAppExtensionType;
