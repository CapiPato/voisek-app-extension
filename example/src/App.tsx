import React, { useEffect, useState } from 'react';
import {
  Button,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from 'react-native';
import { VoisekAppExtension } from 'react-native-voisek-app-extension';

export default function App() {
  const [isInitialized, setIsInitialized] = useState(false);
  const [isBlockListEnable, setIsBlockListEnable] = useState(false);
  const [inputNumber, onChangeInputNumber] = useState<string>('');
  const toggleSwitchBlockList = () =>
    setIsBlockListEnable((previousState) => !previousState);

  useEffect(() => {
    VoisekAppExtension.doActiveBlockCallOnList(isBlockListEnable);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isBlockListEnable]);

  function TestInitialize() {
    VoisekAppExtension.initCallService(
      true,
      () => {
        VoisekAppExtension.setNotificationData(
          2100,
          '',
          '',
          '',
          '',
          '',
          '',
          '',
          ''
        );
        setIsInitialized(true);
      },
      () => {
        setIsInitialized(false);
      }
    );
  }

  async function TestAddNumbers() {
    await VoisekAppExtension.addBlockingPhoneNumbers([
      {
        category: 'block',
        number: inputNumber,
      },
    ]);
  }

  return (
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
        onChangeText={onChangeInputNumber}
        value={inputNumber}
        placeholder="Add Number"
        keyboardType="phone-pad"
      />
      <View style={styles.smallSpace} />
      <Button
        onPress={() => {
          TestAddNumbers();
        }}
        title="Add Number"
        color="#D0021B"
        accessibilityLabel="Do Add Number"
        disabled={!isInitialized}
      />
      <View style={styles.normalSpace} />
      <Text style={styles.body}>{`Block: ${inputNumber}`}</Text>
      <View style={styles.smallSpace} />
      <Switch
        onValueChange={toggleSwitchBlockList}
        value={isBlockListEnable}
        disabled={!isInitialized && inputNumber !== ''}
      />
    </View>
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
