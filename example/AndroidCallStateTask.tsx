import { VoisekAppExtension } from 'react-native-voisek-app-extension';

type CallData = {
  phoneNumber?: string;
  event?: string;
};

async function CallStateTask({ phoneNumber, event }: CallData) {
  console.log(event, phoneNumber);
  VoisekAppExtension.cancelNotifications();
  if (event === 'incoming') {
    VoisekAppExtension.showAFullScreenNotification(
      `${event}`,
      `${event}: ${phoneNumber}`
    );
  }
}

export default async (data: CallData) => {
  // do stuff
  await CallStateTask(data);
};
