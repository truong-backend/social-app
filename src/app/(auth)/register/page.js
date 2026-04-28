"use client";

import { useState, useRef, useEffect, useCallback, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Image from "next/image";
import { Eye, EyeOff, ArrowDown, ArrowLeftRight } from "lucide-react";
import { AnimatePresence, motion } from "framer-motion";
import useMeasure from "react-use-measure";
import MotionContainer from "@/components/ui-components/MotionContainer";
import Button from "@/components/ui-components/Button";
import Link from "next/link";
import api, { setAuthToken } from "@/utils/axios";
import { jwtDecode } from "jwt-decode";

// Helper functions
const parseApiError = (error) => {
  if (error.response) {
    return error.response.data?.message || error.response.data?.error || `Lỗi server (${error.response.status})`;
  }
  return error.request ? "Không thể kết nối đến server. Vui lòng thử lại." : error.message || "Lỗi không xác định";
};

const validateForm = (mode, formData) => {
  const { email, password, confirmPassword, givenName, familyName, birthdate } = formData;
  if (!email || !password) return "❌ Vui lòng điền đầy đủ thông tin";
  if (mode === "register") {
    if (password !== confirmPassword) return "❌ Mật khẩu không khớp!";
    if (!givenName || !familyName || !birthdate) return "❌ Vui lòng điền đầy đủ thông tin";
  }
  return null;
};

const formatLockoutTime = (timeString) => {
  try {
    return new Date(timeString).toLocaleString("vi-VN", {
      day: "2-digit", month: "2-digit", year: "numeric",
      hour: "2-digit", minute: "2-digit", second: "2-digit",
    });
  } catch {
    return timeString;
  }
};

// Message Component
const MessageDisplay = ({ message, verifyMessage, verifying }) => {
  const getMessageClass = (msg) => {
    if (msg?.includes("✅")) return "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200";
    if (msg?.includes("⚠️")) return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200";
    if (msg?.includes("🔒")) return "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200";
    return "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200";
  };
  return (
    <>
      {verifyMessage && <div className={`p-3 text-sm rounded mb-4 ${getMessageClass(verifyMessage)}`}>{verifyMessage}</div>}
      {message && <div className={`p-3 text-sm rounded mb-4 ${getMessageClass(message)}`}>{message}</div>}
      {verifying && <div className="p-3 text-sm rounded mb-4 bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">🔄 Đang xác thực email...</div>}
    </>
  );
};

// Form Fields Component
const FormFields = ({ mode, formData, setFormData, showPassword, setShowPassword, loading, verifying, showConfirmPassword, setShowConfirmPassword }) => {
  const handleInputChange = (field) => (e) => setFormData(prev => ({ ...prev, [field]: e.target.value }));
  const isDisabled = loading || verifying;
  return (
    <>
      <div className="space-y-2 mb-2">
        <h4 className="text-sm font-medium text-muted-foreground">Email</h4>
        <input type="email" value={formData.email} onChange={handleInputChange("email")}
          className="w-full bg-transparent border-b border-input px-0 py-1 focus:outline-none focus:border-primary text-foreground"
          required disabled={isDisabled} />
      </div>
      {mode === "register" && (
        <div className="space-y-4">
          <div className="flex space-x-4">
            <div className="space-y-2 flex-1">
              <h4 className="text-sm font-medium text-muted-foreground">Tên</h4>
              <input type="text" value={formData.givenName} onChange={handleInputChange("givenName")}
                className="w-full bg-transparent border-b border-input px-0 py-1 focus:outline-none focus:border-primary text-foreground"
                required disabled={loading} />
            </div>
            <div className="space-y-2 flex-1">
              <h4 className="text-sm font-medium text-muted-foreground">Họ</h4>
              <input type="text" value={formData.familyName} onChange={handleInputChange("familyName")}
                className="w-full bg-transparent border-b border-input px-0 py-1 focus:outline-none focus:border-primary text-foreground"
                required disabled={loading} />
            </div>
          </div>
          <div className="space-y-2">
            <h4 className="text-sm font-medium text-muted-foreground">Ngày sinh</h4>
            <input type="date" value={formData.birthdate} onChange={handleInputChange("birthdate")}
              className="w-full bg-transparent border-b border-input px-0 py-1 focus:outline-none focus:border-primary text-foreground"
              required disabled={loading} />
          </div>
        </div>
      )}
      <div className="space-y-2 relative">
        <h4 className="text-sm font-medium text-muted-foreground">Mật khẩu</h4>
        <input type={showPassword ? "text" : "password"} value={formData.password} onChange={handleInputChange("password")}
          className="w-full bg-transparent border-b border-input px-0 py-1 focus:outline-none focus:border-primary pr-10 text-foreground"
          required minLength={8} disabled={isDisabled} />
        <p className="text-gray-500 text-sm">
          Mật khẩu phải có tối thiểu 8 kí tự, bao gồm ít nhất 1 chữ cái thường, 1 chữ cái hoa, 1 chữ số và kí tự đặc biệt @ $ ! % * ? &
        </p>
        <button type="button" className="absolute right-0 top-7 p-1 text-muted-foreground hover:text-foreground"
          onClick={() => setShowPassword(prev => !prev)} tabIndex={-1} disabled={isDisabled}>
          {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
        </button>
      </div>
      {mode === "register" && (
        <div className="space-y-2 relative">
          <h4 className="text-sm font-medium text-muted-foreground">Nhập lại mật khẩu</h4>
          <input type={showConfirmPassword ? "text" : "password"} value={formData.confirmPassword}
            onChange={handleInputChange("confirmPassword")}
            className="w-full bg-transparent border-b border-input px-0 py-1 focus:outline-none focus:border-primary pr-10 text-foreground"
            required minLength={6} disabled={loading} />
          <button type="button" className="absolute right-0 top-7 p-1 text-muted-foreground hover:text-foreground"
            onClick={() => setShowConfirmPassword(prev => !prev)} tabIndex={-1} disabled={loading}>
            {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        </div>
      )}
    </>
  );
};

// ============================================================
// Google Login Button — luôn hiển thị, không phụ thuộc renderButton //
// ============================================================
const GoogleLoginButton = ({ onSuccess, onError, loading }) => {
  const [gsiReady, setGsiReady] = useState(false);

  useEffect(() => {
    const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
    if (!clientId) return;

    const initGSI = () => {
      if (!window.google?.accounts?.id) return;
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: (response) => {
          if (response.credential) onSuccess(response.credential);
          else onError("Không nhận được credential từ Google");
        },
        auto_select: false,
        cancel_on_tap_outside: true,
      });
      setGsiReady(true);
    };

    if (window.google?.accounts?.id) { initGSI(); return; }

    const existing = document.querySelector('script[src="https://accounts.google.com/gsi/client"]');
    if (existing) { existing.addEventListener("load", initGSI); return; }

    const script = document.createElement("script");
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.onload = initGSI;
    document.head.appendChild(script);
  }, [onSuccess, onError]);

  const handleClick = () => {
    if (!gsiReady || !window.google?.accounts?.id) {
      onError("Google chưa sẵn sàng, vui lòng thử lại sau giây lát");
      return;
    }
    window.google.accounts.id.prompt((notification) => {
      if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
        onError("Popup bị chặn. Hãy cho phép popup từ trang này và thử lại.");
      }
    });
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={loading}
      className="w-full flex items-center justify-center gap-3 px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors duration-200 text-gray-700 dark:text-gray-200 font-medium text-sm disabled:opacity-50 disabled:cursor-not-allowed"
    >
      <svg width="20" height="20" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
        <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
        <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
        <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
        <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
        <path fill="none" d="M0 0h48v48H0z"/>
      </svg>
      {loading ? "Đang xử lý..." : "Đăng nhập với Google"}
    </button>
  );
};

// Loading fallback
const AuthPageLoading = () => (
  <div className="min-h-screen bg-background text-foreground flex flex-col">
    <main className="flex-grow flex flex-col md:flex-row h-full">
      <div className="w-full md:w-1/2 h-screen flex items-center justify-center bg-muted relative">
        <Image src="/Connect.png" alt="Network illustration" width={400} height={400} className="max-w-full h-auto object-contain" priority />
      </div>
      <div className="w-full md:w-1/2 min-h-screen flex items-center justify-center p-6 bg-background">
        <div className="w-full max-w-md text-card-foreground rounded-xl p-8 shadow-xl bg-[var(--card)]">
          <div className="flex justify-center items-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
            <span className="ml-2 text-muted-foreground">Đang tải...</span>
          </div>
        </div>
      </div>
    </main>
  </div>
);

// ============================================================
// Main Component
// ============================================================
function AuthPageContent() {
  const [mode, setMode] = useState("login");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [showResendButton, setShowResendButton] = useState(false);
  const [formData, setFormData] = useState({
    email: "", password: "", confirmPassword: "", givenName: "", familyName: "", birthdate: "",
  });
  const [messages, setMessages] = useState({ verify: "", general: "" });
  const [status, setStatus] = useState({ verifying: false, loading: false, googleLoading: false });

  const formRef = useRef(null);
  const searchParams = useSearchParams();
  const router = useRouter();
  const [formBoundsRef, { height }] = useMeasure();

  const clearForm = () => {
    setFormData({ email: "", password: "", confirmPassword: "", givenName: "", familyName: "", birthdate: "" });
    setShowResendButton(false);
  };

  const handleResend = async () => {
    const email = searchParams.get("email") || formData.email;
    if (!email) { setMessages(prev => ({ ...prev, general: "❌ Vui lòng nhập email trước khi gửi lại" })); return; }
    setStatus(prev => ({ ...prev, loading: true }));
    try {
      const res = await api.post(`/v1/register/resend-email?email=${email}`);
      if (res.data.code === 200) {
        setMessages(prev => ({ ...prev, general: "✅ Đã gửi lại email xác thực! Vui lòng kiểm tra hộp thư." }));
        setShowResendButton(false);
      }
    } catch (error) {
      setMessages(prev => ({ ...prev, general: `❌ Gửi lại email thất bại: ${parseApiError(error)}` }));
    } finally {
      setStatus(prev => ({ ...prev, loading: false }));
    }
  };

  // Email verification từ URL params
  useEffect(() => {
    const verifyEmail = async () => {
      const emailParam = searchParams.get("email");
      const codeParam = searchParams.get("code");
      if (!emailParam || !codeParam) return;
      setStatus(prev => ({ ...prev, verifying: true }));
      try {
        const res = await api.patch("/v1/register/verify",
          { email: emailParam, code: codeParam },
          { headers: { "Content-Type": "application/json" }, timeout: 10000 }
        );
        if (res.data.code === 200) {
          setMessages(prev => ({ ...prev, verify: "✅ Xác thực email thành công! Bạn có thể đăng nhập." }));
          setMode("login");
        }
      } catch (error) {
        if (error.response?.data?.code === 1009) {
          setMessages(prev => ({ ...prev, verify: "❌ Mã xác thực hết hạn hoặc không hợp lệ" }));
          setShowResendButton(true);
        } else {
          setMessages(prev => ({ ...prev, verify: `❌ Xác thực thất bại: ${parseApiError(error)}` }));
        }
      } finally {
        setStatus(prev => ({ ...prev, verifying: false }));
      }
    };
    verifyEmail();
  }, [searchParams]);

  const handleRegister = async () => {
    setStatus(prev => ({ ...prev, loading: true }));
    try {
      const res = await api.post("/v1/register", {
        email: formData.email, password: formData.password,
        givenName: formData.givenName, familyName: formData.familyName, birthdate: formData.birthdate,
      });
      if (res.data.code === 200) {
        setMessages(prev => ({ ...prev, general: "✅ Đăng ký thành công! Vui lòng kiểm tra email để xác thực." }));
        setMode("login");
        clearForm();
      }
    } catch (error) {
      const code = error.response?.data?.code;
      if (code === 2009) {
        setMessages(prev => ({ ...prev, general: "❌ Email chưa xác thực, vui lòng kiểm tra email của bạn" }));
        setShowResendButton(true);
      } else if (code === 1012) {
        setMessages(prev => ({ ...prev, general: "❌ Email này đã được đăng ký" }));
      } else {
        setMessages(prev => ({ ...prev, general: `❌ Đăng ký thất bại: ${parseApiError(error)}` }));
      }
    } finally {
      setStatus(prev => ({ ...prev, loading: false }));
    }
  };

  const handleLogin = async () => {
    setStatus(prev => ({ ...prev, loading: true }));
    try {
      const res = await api.post("/v1/auth/login", { email: formData.email, password: formData.password });
      if (res.data.code === 200 && res.data.body.token) {
        const token = res.data.body.token;
        const decoded = jwtDecode(token);
        const authData = { role: decoded.scope, accessToken: token, userId: decoded.sub, userName: decoded.username };
        Object.entries(authData).forEach(([key, value]) => localStorage.setItem(key, value));
        if (setAuthToken(token, decoded.sub, decoded.username)) {
          setMessages(prev => ({ ...prev, general: "✅ Đăng nhập thành công!" }));
          setFormData(prev => ({ ...prev, email: "", password: "" }));
          setTimeout(() => window.location.href = "/home", 500);
        } else {
          setMessages(prev => ({ ...prev, general: "⚠️ Đăng nhập thành công nhưng có lỗi khi đồng bộ hóa phiên làm việc" }));
          setTimeout(() => router.push("/index"), 1200);
        }
      } else if (res.data.code === 1003) {
        const remainingAttempts = res.data.body?.remainingAttempts || 0;
        setMessages(prev => ({ ...prev, general: `❌ Thông tin đăng nhập không chính xác. Còn lại ${remainingAttempts} lần thử.` }));
      } else if (res.data.code === 1002) {
        const lockoutTime = formatLockoutTime(res.data.body?.time);
        setMessages(prev => ({ ...prev, general: `🔒 Tài khoản tạm thời bị khóa. Thời gian mở khóa: ${lockoutTime}` }));
      } else {
        setMessages(prev => ({ ...prev, general: `❌ ${res.data.message || "Đăng nhập thất bại"}` }));
      }
    } catch (error) {
      const errorData = error.response?.data;
      if (errorData?.code === 1003) {
        const remainingAttempts = errorData.body?.remainingAttempts || 0;
        setMessages(prev => ({ ...prev, general: `❌ Thông tin đăng nhập không chính xác. Còn lại ${remainingAttempts} lần thử.` }));
      } else if (errorData?.code === 1002) {
        const lockoutTime = formatLockoutTime(errorData.body?.time);
        setMessages(prev => ({ ...prev, general: `🔒 Tài khoản tạm thời bị khóa. Thời gian mở khóa: ${lockoutTime}` }));
      } else {
        setMessages(prev => ({ ...prev, general: `❌ Đăng nhập thất bại: ${parseApiError(error)}` }));
      }
    } finally {
      setStatus(prev => ({ ...prev, loading: false }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessages(prev => ({ ...prev, general: "" }));
    const validationError = validateForm(mode, formData);
    if (validationError) { setMessages(prev => ({ ...prev, general: validationError })); return; }
    mode === "register" ? await handleRegister() : await handleLogin();
  };

  // ============================================================
  // Google OAuth Handler
  // ============================================================
  const handleGoogleSuccess = useCallback(async (idToken) => {
    setStatus(prev => ({ ...prev, googleLoading: true }));
    setMessages(prev => ({ ...prev, general: "" }));
    try {
      const res = await api.post("/v1/auth/google", { idToken });
      const token = res.data.body?.token;
      if (!token) throw new Error("Không nhận được token từ server");
      const decoded = jwtDecode(token);
      const authData = { role: decoded.scope, accessToken: token, userId: decoded.sub, userName: decoded.username };
      Object.entries(authData).forEach(([key, value]) => localStorage.setItem(key, value));
      setAuthToken(token, decoded.sub, decoded.username);
      setMessages(prev => ({ ...prev, general: "✅ Đăng nhập Google thành công!" }));
      setTimeout(() => window.location.href = "/home", 500);
    } catch (error) {
      setMessages(prev => ({ ...prev, general: `❌ Đăng nhập Google thất bại: ${parseApiError(error)}` }));
    } finally {
      setStatus(prev => ({ ...prev, googleLoading: false }));
    }
  }, []);

  const handleGoogleError = useCallback((errorMsg) => {
    setMessages(prev => ({ ...prev, general: `❌ ${errorMsg}` }));
  }, []);

  const scrollToForm = () => formRef.current?.scrollIntoView({ behavior: "smooth" });
  const toggleMode = () => {
    setMode(prev => prev === "login" ? "register" : "login");
    setMessages({ verify: "", general: "" });
    setShowResendButton(false);
  };

  const isAnyLoading = status.loading || status.googleLoading || status.verifying;

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col">
      <main className="flex-grow flex flex-col md:flex-row h-full">
        {/* Left Side */}
        <div className="w-full md:w-1/2 h-screen flex items-center justify-center bg-muted relative">
          <Image src="/Connect.png" alt="Network illustration" width={400} height={400}
            className="max-w-full h-auto object-contain" priority />
          <div className="absolute bottom-10 left-0 right-0 flex justify-center md:hidden">
            <button onClick={scrollToForm}
              className="flex items-center gap-2 bg-primary text-primary-foreground px-6 py-3 rounded-full shadow-lg hover:opacity-90 transition-opacity">
              Go to {mode} <ArrowDown className="h-4 w-4" />
            </button>
          </div>
        </div>

        {/* Right Side */}
        <div ref={formRef} className="w-full md:w-1/2 min-h-screen flex items-center justify-center p-6 bg-background">
          <div className="w-full max-w-md text-card-foreground rounded-xl p-8 shadow-xl bg-[var(--card)]" style={{ overflow: "hidden" }}>
            <div>
              {showResendButton ? (
                <h1 className="text-2xl font-bold mb-4">Xác thực email</h1>
              ) : (
                <div className="flex justify-between items-center mb-6">
                  <h1 className="text-2xl font-bold">{mode === "login" ? "Đăng nhập" : "Tạo tài khoản mới"}</h1>
                  <button onClick={toggleMode} className="text-sm text-muted-foreground hover:text-foreground transition">
                    <ArrowLeftRight className="inline-block w-4 h-4 mr-1" />
                    {mode === "login" ? "Đăng ký" : "Đăng nhập"}
                  </button>
                </div>
              )}
              <MessageDisplay message={messages.general} verifyMessage={messages.verify} verifying={status.verifying} />
            </div>

            {showResendButton ? (
              <div className="flex flex-col items-center gap-2">
                <Button onClick={handleResend} className="w-full text-md text-white bg-black px-3 py-2 rounded hover:underline">
                  Gửi lại email xác thực 📩
                </Button>
                <p className="text-sm">Hoặc</p>
                <Button onClick={() => { window.location.href = "/register"; }} className="w-full py-2">
                  Đăng nhập
                </Button>
              </div>
            ) : (
              <motion.div animate={{ height }} transition={{ duration: 0.3, ease: "easeInOut" }} style={{ overflow: "hidden" }}>
                <div ref={formBoundsRef}>
                  <AnimatePresence mode="wait">
                    <MotionContainer key={mode} modeKey={mode} effect="fadeUp">
                      <div className="space-y-6">

                        {/* ===== GOOGLE BUTTON ===== */}
                        <GoogleLoginButton
                          onSuccess={handleGoogleSuccess}
                          onError={handleGoogleError}
                          loading={isAnyLoading}
                        />

                        {/* Divider */}
                        <div className="flex items-center gap-3">
                          <div className="flex-1 border-t border-border" />
                          <span className="text-xs text-muted-foreground">hoặc</span>
                          <div className="flex-1 border-t border-border" />
                        </div>
                        {/* ===== END GOOGLE BUTTON ===== */}

                        <form onSubmit={handleSubmit} className="space-y-6">
                          <FormFields
                            mode={mode}
                            formData={formData}
                            setFormData={setFormData}
                            showPassword={showPassword}
                            setShowPassword={setShowPassword}
                            showConfirmPassword={showConfirmPassword}
                            setShowConfirmPassword={setShowConfirmPassword}
                            loading={status.loading}
                            verifying={status.verifying}
                          />

                          <Button type="submit" disabled={isAnyLoading} className="w-full py-2">
                            {status.loading ? "Loading..." : mode === "login" ? "Đăng nhập" : "Đăng ký"}
                          </Button>

                          <div className="mt-6 text-center text-sm text-muted-foreground">
                            <div>
                              Quên mật khẩu?{" "}
                              <Link href="/forgot-password" className="text-blue-500 dark:text-blue-400 hover:underline">
                                Tạo mật khẩu mới
                              </Link>
                            </div>
                          </div>
                        </form>
                      </div>
                    </MotionContainer>
                  </AnimatePresence>
                </div>
              </motion.div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default function AuthPage() {
  return (
    <Suspense fallback={<AuthPageLoading />}>
      <AuthPageContent />
    </Suspense>
  );
}