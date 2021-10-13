import React, { useEffect, useState } from 'react';
import {
  Button,
  FlatList,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from 'react-native';
import {
  VoisekAppExtension,
  VoisekCallStateManager,
} from 'react-native-voisek-app-extension';

export default function App() {
  const [isInitialized, setIsInitialized] = useState(false);
  const [isBlockListEnable, setIsBlockListEnable] = useState(false);
  const [callDetection, setCallDetecion] = useState(false);
  const [currentCallDetector, setCallDetector] = useState<
    VoisekCallStateManager | undefined
  >(undefined);
  const [callDetectionResult, setCallDetectionResult] = useState<
    { event: string; number: string }[]
  >([]);
  const [newState, setNewState] = useState<{ event: string; number: string }>();
  const [inputNumber, onChangeInputNumber] = useState<string>('');
  const toggleSwitchBlockList = () =>
    setIsBlockListEnable((previousState) => !previousState);

  function CallStateCheck(event: string, number: string) {
    setNewState({ event, number });
  }

  function addCallDetectionEvent(addState: { event: string; number: string }) {
    const newResult = [...callDetectionResult];
    newResult.push(addState);
    console.log('newResult', newResult);
    setCallDetectionResult(newResult);
  }

  function addCallDetector() {
    if (callDetection === true && currentCallDetector === undefined) {
      console.log('Start: callDetection');
      const tempCallDetector = new VoisekCallStateManager(CallStateCheck);
      setCallDetector(tempCallDetector);
    }
  }

  useEffect(() => {
    VoisekAppExtension.doActiveBlockCallOnList(isBlockListEnable);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isBlockListEnable]);

  useEffect(() => {
    if (newState !== undefined) {
      addCallDetectionEvent(newState);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [newState]);

  useEffect(() => {
    return function cleanup() {
      if (currentCallDetector !== undefined) {
        currentCallDetector.dispose();
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentCallDetector]);

  useEffect(() => {
    if (callDetection === true) {
      addCallDetector();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [callDetection]);

  function TestInitialize() {
    VoisekAppExtension.initCallService(
      true,
      () => {
        console.log('SUCCESS');
        setIsInitialized(true);
      },
      () => {
        console.log('FAIL');
        setIsInitialized(false);
      }
    );
  }

  async function TestAddNumbers() {
    const addBlockingPhoneNumbers =
      await VoisekAppExtension.addBlockingPhoneNumbers([
        {
          category: 'block',
          number: inputNumber,
        },
      ]);
    console.log('addBlockingPhoneNumbers', addBlockingPhoneNumbers);
  }

  async function TestCallDetection() {
    setCallDetecion(true);
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
      <Button
        onPress={() => {
          TestCallDetection();
        }}
        title="Call Detection"
        color="#D0021B"
        accessibilityLabel="Start Call Detection"
        disabled={!isInitialized}
      />
      <View style={styles.smallSpace} />
      <View style={styles.callDetectSquare}>
        <FlatList
          keyExtractor={(_item, index) => index.toString()}
          style={styles.list}
          data={callDetectionResult}
          renderItem={({ item }) => {
            return <Text>{`${item.event}: ${item.number}`}</Text>;
          }}
        />
      </View>
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
