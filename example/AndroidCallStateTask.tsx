type CallData = {
  phoneNumber: string;
  event: string;
};

function CallStateTask({ phoneNumber, event }: CallData) {
  console.log(event, phoneNumber);
  return Promise.resolve();
}

export default CallStateTask;
