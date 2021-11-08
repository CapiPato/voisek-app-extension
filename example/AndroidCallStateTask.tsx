import { VoisekAppExtension } from 'react-native-voisek-app-extension';

type CallData = {
  phoneNumber?: string;
  event?: string;
};

async function CallStateTask({ phoneNumber, event }: CallData) {
  console.log(event, phoneNumber);
  VoisekAppExtension.cancelNotifications();
  if (event !== null && event !== undefined) {
    if (event === 'incoming') {
      VoisekAppExtension.showAFullScreenNotification(
        `${event}`,
        `${event}: ${phoneNumber}`
      );
    }
  } else {
    VoisekAppExtension.showAFullScreenNotification(
      'Voisek System',
      'Going Background'
    );
  }
  return Promise.resolve();
}

export default async (data: CallData) => {
  // do stuff
  await CallStateTask(data);
};
