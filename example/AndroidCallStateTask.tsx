import { VoisekAppExtension } from 'react-native-voisek-app-extension';

type CallData = {
  phoneNumber?: string;
  event?: string;
};

async function CallStateTask({ phoneNumber, event }: CallData) {
  console.log(event, phoneNumber);
  VoisekAppExtension.cancelNotifications();
  if (
    event !== null ||
    event !== undefined ||
    phoneNumber !== null ||
    phoneNumber !== undefined
  ) {
    if (event === 'incoming') {
      VoisekAppExtension.showAFullScreenNotification(
        `${event}`,
        `${event}: ${phoneNumber}`
      );
    }
    return Promise.resolve();
  }
}

export default async (data: CallData) => {
  // do stuff
  await CallStateTask(data);
};
