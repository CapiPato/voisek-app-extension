import React, { useEffect, useState } from 'react';
import {
  Button,
  Keyboard,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  TouchableWithoutFeedback,
  View,
} from 'react-native';
import { VoisekAppExtension } from 'react-native-voisek-app-extension';

export default function App() {
  const [isInitialized, setIsInitialized] = useState(false);
  const [isBlockListEnable, setIsBlockListEnable] = useState(false);
  const [inputBlockingNumber, onChangeInputBlockingNumber] =
    useState<string>('523344458500');
  const [inputSpamNumber, onChangeInputSpamNumber] =
    useState<string>('523344458500');
  const toggleSwitchBlockList = () =>
    setIsBlockListEnable((previousState) => !previousState);

  useEffect(() => {
    if (isInitialized) {
      VoisekAppExtension.doActiveBlockCallOnList(isBlockListEnable);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isBlockListEnable]);

  function TestInitialize() {
    VoisekAppExtension.initCallService(
      'group.voisekdata',
      'com.voisekappexample.CallDirectoryHandler',
      true,
      () => {
        VoisekAppExtension.setNotificationData('', '', '', '', '', '', '', '');
        setIsInitialized(true);
      },
      () => {
        setIsInitialized(false);
      }
    );
  }

  async function TestAddBlockingNumbers() {
    const response = await VoisekAppExtension.addBlockingPhoneNumbers([
      {
        category: 'block',
        phoneNumber: inputBlockingNumber,
      },
    ]);
    console.log('TestAddBlockingNumbers', response);
  }

  async function TestAddSpamNumbers() {
    const response = await VoisekAppExtension.addSpamPhoneNumbers([
      {
        label: 'NUMERO DE SPAM',
        phoneNumber: inputSpamNumber,
      },
    ]);
    console.log('TestAddSpamNumbers', response);
  }

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss} accessible={false}>
      <View style={styles.container}>
        <Text style={styles.title}>
          <Text style={styles.titleRed}>{'VoiSek App '}</Text>
          {'Extension - Test'}
        </Text>
        <View style={styles.normalSpace} />
        <Button
          onPress={() => {
            TestInitialize();
          }}
          title="Initialize"
          color="#D0021B"
          accessibilityLabel="Initialized"
        />
        <View style={styles.normalSpace} />
        <TextInput
          style={styles.input}
          onChangeText={onChangeInputBlockingNumber}
          value={inputBlockingNumber}
          placeholder="Add Blocking Number"
          keyboardType="phone-pad"
        />
        <View style={styles.smallSpace} />
        <Button
          onPress={() => {
            TestAddBlockingNumbers();
          }}
          title="Add Blocking Number"
          color="#D0021B"
          accessibilityLabel="Do Add Blocking Number"
          disabled={!isInitialized}
        />
        <View style={styles.smallSpace} />
        <Text style={styles.body}>{`Do Block: ${inputBlockingNumber}`}</Text>
        <View style={styles.smallSpace} />
        <Switch
          onValueChange={toggleSwitchBlockList}
          value={isBlockListEnable}
          disabled={!isInitialized && inputBlockingNumber !== ''}
        />
        <View style={styles.normalSpace} />
        <TextInput
          style={styles.input}
          onChangeText={onChangeInputSpamNumber}
          value={inputSpamNumber}
          placeholder="Add Spam Number"
          keyboardType="phone-pad"
        />
        <View style={styles.smallSpace} />
        <Button
          onPress={() => {
            TestAddSpamNumbers();
          }}
          title="Add Spam Number"
          color="#D0021B"
          accessibilityLabel="Do Add Spam Number"
          disabled={!isInitialized}
        />
      </View>
    </TouchableWithoutFeedback>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  title: {
    fontSize: 20,
    color: '#2F2E2E',
  },
  titleRed: {
    fontSize: 20,
    color: '#D0021B',
  },
  body: {
    fontSize: 16,
    color: '#2F2E2E',
  },
  input: {
    width: '70%',
    height: 40,
    margin: 12,
    borderWidth: 1,
    padding: 10,
  },
  normalSpace: {
    width: '100%',
    height: 20,
  },
  smallSpace: {
    width: '100%',
    height: 5,
  },
  callDetectSquare: {
    width: '70%',
    height: 200,
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 20,
    borderWidth: 2,
    borderColor: '#2F2E2E',
  },
  list: { width: '100%', height: '100%' },
});
