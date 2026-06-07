import api from "@/utils/axios";

export const groupApi = {
  createGroup: (name, memberUsernames) =>
    api.post("/v1/group", { name, memberUsernames }),

  updateGroup: (chatId, name, avatarFile) => {
    const form = new FormData();
    form.append("chatId", chatId);
    if (name) form.append("name", name);
    if (avatarFile) form.append("avatar", avatarFile);
    return api.put("/v1/group", form);
  },

  addMembers: (chatId, usernames) =>
    api.post("/v1/group/members/add", { chatId, usernames }),

  removeMember: (chatId, username) =>
    api.delete("/v1/group/members/remove", { data: { chatId, username } }),

  leaveGroup: (chatId) => api.post(`/v1/group/${chatId}/leave`),

  dissolveGroup: (chatId) => api.delete(`/v1/group/${chatId}/dissolve`),

  promoteToAdmin: (chatId, username) =>
    api.post("/v1/group/members/promote", { chatId, username }),

  demoteToMember: (chatId, username) =>
    api.post("/v1/group/members/demote", { chatId, username }),

  transferOwnership: (chatId, username) =>
    api.post("/v1/group/members/transfer-ownership", { chatId, username }),

  getMembers: (chatId) => api.get(`/v1/group/${chatId}/members`),

  pinMessage: (chatId, messageId) =>
    api.post(`/v1/group/${chatId}/pin/${messageId}`),

  unpinMessage: (chatId, messageId) =>
    api.delete(`/v1/group/${chatId}/pin/${messageId}`),

  getPinnedMessages: (chatId) => api.get(`/v1/group/${chatId}/pins`),

  sendGroupMessage: (chatId, text) =>
    api.post(`/v1/chat/group/${chatId}/send`, { text }),

  sendGroupFile: (chatId, file) => {
    const form = new FormData();
    form.append("attachment", file);
    return api.post(`/v1/chat/group/${chatId}/send-file`, form);
  },

  // ✅ Thêm mới: gửi voice vào nhóm
  sendGroupVoice: (chatId, blob) => {
    const form = new FormData();
    form.append("voiceFile", blob);
    return api.post(`/v1/chat/group/${chatId}/send-voice`, form);
  },
};