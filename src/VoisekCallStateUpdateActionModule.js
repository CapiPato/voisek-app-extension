var VoisekCallStateUpdateActionModule = {
  callStateUpdated(state, incomingNumber) {
    this.callback && this.callback(state, incomingNumber);
  },
};
module.exports = VoisekCallStateUpdateActionModule;
