export interface RegisterRequest {
  email: string
  password: string
  familyName: string
  givenName: string
  birthdate: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface ConfirmEmailRequest {
  code: string
}

export interface PrepareResetPasswordRequest {
  email: string
}

export interface ConfirmResetCodeRequest {
  code: string
}

export interface UpdatePasswordRequest {
  newPassword: string
  confirmPassword: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  accountId: string
  userId: string
  role: string
}

export interface RegisterResponse {
  accountId: string
  email: string
  message: string
}