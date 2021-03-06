import { AppRegistry } from 'react-native';

import AndroidCallStateTask from './AndroidCallStateTask';
import { name as appName } from './app.json';
import App from './src/App';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.registerHeadlessTask(
  'AndroidCallStateTask',
  () => AndroidCallStateTask
);
