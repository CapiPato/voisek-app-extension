import { VoisekAppExtension } from 'react-native-voisek-app-extension';

type CallData = {
  phoneNumber?: string;
  event?: string;
};

async function CallStateTask({ phoneNumber, event }: CallData) {
  console.log(event, phoneNumber);
  VoisekAppExtension.cancelNotifications(0);
  if (event !== null && event !== undefined) {
    if (event === 'incoming') {
      VoisekAppExtension.showAFullScreenNotification(
        `${event}`,
        `${event}: ${phoneNumber}`,
        1800
      );
    } else {
      VoisekAppExtension.showAFullScreenNotification(
        'Voisek System',
        'Going Background',
        500
      );
      VoisekAppExtension.cancelNotifications(1000);
    }
  } else {
    VoisekAppExtension.showAFullScreenNotification(
      'Voisek System',
      'Going Background',
      500
    );
    VoisekAppExtension.cancelNotifications(1000);
  }
  return Promise.resolve();
}

export default async (data: CallData) => {
  // do stuff
  await CallStateTask(data);
};
