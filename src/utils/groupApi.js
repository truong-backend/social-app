import api from "@/utils/axios";

export const groupApi = {
  // Tạo nhóm
  createGroup: (name, memberUsernames) =>
    api.post("/v1/group", { name, memberUsernames }),

  // Cập nhật thông tin nhóm (multipart nếu có avatar)
  updateGroup: (chatId, name, avatarFile) => {
    const form = new FormData();
    form.append("chatId", chatId);
    if (name) form.append("name", name);
    if (avatarFile) form.append("avatar", avatarFile);
    return api.put("/v1/group", form);
  },

  // Thêm thành viên
  addMembers: (chatId, usernames) =>
    api.post("/v1/group/members/add", { chatId, usernames }),

  // Xóa thành viên
  removeMember: (chatId, username) =>
    api.delete("/v1/group/members/remove", { data: { chatId, username } }),

  // Rời nhóm
  leaveGroup: (chatId) => api.post(`/v1/group/${chatId}/leave`),

  // Giải tán nhóm (Owner only)
  dissolveGroup: (chatId) => api.delete(`/v1/group/${chatId}/dissolve`),

  // Nâng Admin
  promoteToAdmin: (chatId, username) =>
    api.post("/v1/group/members/promote", { chatId, username }),

  // Hạ xuống Member
  demoteToMember: (chatId, username) =>
    api.post("/v1/group/members/demote", { chatId, username }),

  // Chuyển quyền trưởng nhóm
  transferOwnership: (chatId, username) =>
    api.post("/v1/group/members/transfer-ownership", { chatId, username }),

  // Lấy danh sách thành viên
  getMembers: (chatId) => api.get(`/v1/group/${chatId}/members`),

  // Ghim tin nhắn
  pinMessage: (chatId, messageId) =>
    api.post(`/v1/group/${chatId}/pin/${messageId}`),

  // Bỏ ghim tin nhắn
  unpinMessage: (chatId, messageId) =>
    api.delete(`/v1/group/${chatId}/pin/${messageId}`),

  // Lấy danh sách tin nhắn đã ghim
  getPinnedMessages: (chatId) => api.get(`/v1/group/${chatId}/pins`),

  // Gửi tin nhắn vào group
  sendGroupMessage: (chatId, text) =>
    api.post(`/v1/chat/group/${chatId}/send`, { text }),

  // Gửi file vào group
  sendGroupFile: (chatId, file) => {
    const form = new FormData();
    form.append("attachment", file);
    return api.post(`/v1/chat/group/${chatId}/send-file`, form);
  },
};