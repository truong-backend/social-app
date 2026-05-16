"use client";

import { useState, useRef, useEffect, Suspense } from "react";
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

const parseApiError = (error) => {
  if (error.response) {
    return (
      error.response.data?.message ||
      error.response.data?.error ||
      `Lỗi server (${error.response.status})`
    );
  }
  return error.request
    ? "Không thể kết nối đến server. Vui lòng thử lại."
    : error.message || "Lỗi không xác định";
};

const validateForm = (mode, formData) => {
  const { email, password, confirmPassword, givenName, familyName, birthdate } =
    formData;
  if (!email || !password) return "❌ Vui lòng điền đầy đủ thông tin";
  if (mode === "register") {
    if (password !== confirmPassword) return "❌ Mật khẩu không khớp!";
    if (!givenName || !familyName || !birthdate)
      return "❌ Vui lòng điền đầy đủ thông tin";
  }
  return null;
};

const formatLockoutTime = (timeString) => {
  try {
    return new Date(timeString).toLocaleString("vi-VN", {
      day: "2-digit", month: "2-digit", year: "numeric",
      hour: "2-digit", minute: "2-digit", second: "2-digit",
    });
  } catch { return timeString; }
};

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

const FormFields = ({ mode, formData, setFormData, showPassword, setShowPassword, loading, verifying, showConfirmPassword, setShowConfirmPassword }) => {
  const handleInputChange = (field) => (e) => setFormData((prev) => ({ ...prev, [field]: e.target.value }));
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
        <p className="text-gray-500 text-sm">Mật khẩu phải có tối thiểu 8 kí tự, bao gồm ít nhất 1 chữ cái thường, 1 chữ cái hoa, 1 chữ số và kí tự đặc biệt @ $ ! % * ? &</p>
        <button type="button" className="absolute right-0 top-7 p-1 text-muted-foreground hover:text-foreground"
          onClick={() => setShowPassword((prev) => !prev)} tabIndex={-1} disabled={isDisabled}>
          {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
        </button>
      </div>
      {mode === "register" && (
        <div className="space-y-2 relative">
          <h4 className="text-sm font-medium text-muted-foreground">Nhập lại mật khẩu</h4>
          <input type={showConfirmPassword ? "text" : "password"} value={formData.confirmPassword} onChange={handleInputChange("confirmPassword")}
            className="w-full bg-transparent border-b border-input px-0 py-1 focus:outline-none focus:border-primary pr-10 text-foreground"
            required minLength={6} disabled={loading} />
          <button type="button" className="absolute right-0 top-7 p-1 text-muted-foreground hover:text-foreground"
            onClick={() => setShowConfirmPassword((prev) => !prev)} tabIndex={-1} disabled={loading}>
            {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        </div>
      )}
    </>
  );
};

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

function AuthPageContent() {
  const [mode, setMode] = useState("login");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [showResendButton, setShowResendButton] = useState(false);
  const [formData, setFormData] = useState({ email: "", password: "", confirmPassword: "", givenName: "", familyName: "", birthdate: "" });
  const [messages, setMessages] = useState({ verify: "", general: "" });
  const [status, setStatus] = useState({ verifying: false, loading: false });

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
    if (!email) { setMessages((prev) => ({ ...prev, general: "❌ Vui lòng nhập email trước khi gửi lại" })); return; }
    setStatus((prev) => ({ ...prev, loading: true }));
    try {
      const res = await api.post(`/v1/register/resend-email?email=${email}`);
      if (res.data.code === 200) {
        setMessages((prev) => ({ ...prev, general: "✅ Đã gửi lại email xác thực! Vui lòng kiểm tra hộp thư." }));
        setShowResendButton(false);
      }
    } catch (error) {
      setMessages((prev) => ({ ...prev, general: `❌ Gửi lại email thất bại: ${parseApiError(error)}` }));
    } finally { setStatus((prev) => ({ ...prev, loading: false })); }
  };

  useEffect(() => {
    const verifyEmail = async () => {
      const emailParam = searchParams.get("email");
      const codeParam = searchParams.get("code");
      if (!emailParam || !codeParam) return;
      setStatus((prev) => ({ ...prev, verifying: true }));
      try {
        const res = await api.patch("/v1/register/verify", { email: emailParam, code: codeParam }, { headers: { "Content-Type": "application/json" }, timeout: 10000 });
        if (res.data.code === 200) { setMessages((prev) => ({ ...prev, verify: "✅ Xác thực email thành công! Bạn có thể đăng nhập." })); setMode("login"); }
      } catch (error) {
        if (error.response?.data?.code === 1009) { setMessages((prev) => ({ ...prev, verify: "❌ Mã xác thực hết hạn hoặc không hợp lệ" })); setShowResendButton(true); }
        else { setMessages((prev) => ({ ...prev, verify: `❌ Xác thực thất bại: ${parseApiError(error)}` })); }
      } finally { setStatus((prev) => ({ ...prev, verifying: false })); }
    };
    verifyEmail();
  }, [searchParams]);

  const handleRegister = async () => {
    setStatus((prev) => ({ ...prev, loading: true }));
    try {
      const res = await api.post("/v1/register", { email: formData.email, password: formData.password, givenName: formData.givenName, familyName: formData.familyName, birthdate: formData.birthdate });
      if (res.data.code === 200) { setMessages((prev) => ({ ...prev, general: "✅ Đăng ký thành công! Vui lòng kiểm tra email để xác thực." })); setMode("login"); clearForm(); }
    } catch (error) {
      const code = error.response?.data?.code;
      if (code === 2009) { setMessages((prev) => ({ ...prev, general: "❌ Email chưa xác thực, vui lòng kiểm tra email của bạn" })); setShowResendButton(true); }
      else if (code === 1012) { setMessages((prev) => ({ ...prev, general: "❌ Email này đã được đăng ký" })); }
      else { setMessages((prev) => ({ ...prev, general: `❌ Đăng ký thất bại: ${parseApiError(error)}` })); }
    } finally { setStatus((prev) => ({ ...prev, loading: false })); }
  };

  const handleLogin = async () => {
    setStatus((prev) => ({ ...prev, loading: true }));
    try {
      const res = await api.post("/v1/auth/login", { email: formData.email, password: formData.password });
      if (res.data.code === 200 && res.data.body.token) {
        const token = res.data.body.token;
        const decoded = jwtDecode(token);
        const authData = { role: decoded.scope, accessToken: token, userId: decoded.sub, userName: decoded.username };
        Object.entries(authData).forEach(([key, value]) => localStorage.setItem(key, value));
        if (setAuthToken(token, decoded.sub, decoded.username)) {
          try {
            const profileRes = await api.get(`/v1/users/${decoded.username}`);
            const profile = profileRes.data?.body || profileRes.data;
            if (profile?.givenName) localStorage.setItem("givenName", profile.givenName);
            if (profile?.familyName) localStorage.setItem("familyName", profile.familyName);
            if (profile?.profilePictureUrl) localStorage.setItem("profilePictureUrl", profile.profilePictureUrl);
          } catch (_) {}
          setMessages((prev) => ({ ...prev, general: "✅ Đăng nhập thành công!" }));
          setFormData((prev) => ({ ...prev, email: "", password: "" }));
          setTimeout(() => (window.location.href = "/home"), 500);
        }
      }
    } catch (error) {
      const errorData = error.response?.data;
      if (errorData?.code === 1003) { const r = errorData.body?.remainingAttempts || 0; setMessages((prev) => ({ ...prev, general: `❌ Thông tin đăng nhập không chính xác. Còn lại ${r} lần thử.` })); }
      else if (errorData?.code === 1002) { const t = formatLockoutTime(errorData.body?.time); setMessages((prev) => ({ ...prev, general: `🔒 Tài khoản tạm thời bị khóa. Thời gian mở khóa: ${t}` })); }
      else { setMessages((prev) => ({ ...prev, general: `❌ Đăng nhập thất bại: ${parseApiError(error)}` })); }
    } finally { setStatus((prev) => ({ ...prev, loading: false })); }
  };

  // ============================================================
  // GOOGLE LOGIN — redirect về BE, BE xử lý OAuth2 flow
  // ============================================================
  const handleGoogleLogin = () => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL;
    window.location.href = `${apiUrl}/oauth2/authorization/google`;
  };
  // ============================================================

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessages((prev) => ({ ...prev, general: "" }));
    const validationError = validateForm(mode, formData);
    if (validationError) { setMessages((prev) => ({ ...prev, general: validationError })); return; }
    mode === "register" ? await handleRegister() : await handleLogin();
  };

  const scrollToForm = () => formRef.current?.scrollIntoView({ behavior: "smooth" });
  const toggleMode = () => { setMode((prev) => (prev === "login" ? "register" : "login")); setMessages({ verify: "", general: "" }); setShowResendButton(false); };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col">
      <main className="flex-grow flex flex-col md:flex-row h-full">
        <div className="w-full md:w-1/2 h-screen flex items-center justify-center bg-muted relative">
          <Image src="/Connect.png" alt="Network illustration" width={400} height={400} className="max-w-full h-auto object-contain" priority />
          <div className="absolute bottom-10 left-0 right-0 flex justify-center md:hidden">
            <button onClick={scrollToForm} className="flex items-center gap-2 bg-primary text-primary-foreground px-6 py-3 rounded-full shadow-lg hover:opacity-90 transition-opacity">
              Go to {mode} <ArrowDown className="h-4 w-4" />
            </button>
          </div>
        </div>
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
                <Button onClick={handleResend} className="w-full text-md text-white bg-black px-3 py-2 rounded hover:underline">Gửi lại email xác thực 📩</Button>
                <p className="text-sm">Hoặc</p>
                <Button onClick={() => { window.location.href = "/register"; }} className="w-full py-2">Đăng nhập</Button>
              </div>
            ) : (
              <motion.div animate={{ height }} transition={{ duration: 0.3, ease: "easeInOut" }} style={{ overflow: "hidden" }}>
                <div ref={formBoundsRef}>
                  <AnimatePresence mode="wait">
                    <MotionContainer key={mode} modeKey={mode} effect="fadeUp">
                      <form onSubmit={handleSubmit} className="space-y-6">
                        <FormFields mode={mode} formData={formData} setFormData={setFormData}
                          showPassword={showPassword} setShowPassword={setShowPassword}
                          showConfirmPassword={showConfirmPassword} setShowConfirmPassword={setShowConfirmPassword}
                          loading={status.loading} verifying={status.verifying} />

                        <Button type="submit" disabled={status.loading || status.verifying} className="w-full py-2">
                          {status.loading ? "Loading..." : mode === "login" ? "Đăng nhập" : "Đăng ký"}
                        </Button>

                        {/* ===== GOOGLE LOGIN BUTTON ===== */}
                        <div className="relative flex items-center py-1">
                          <div className="flex-grow border-t border-input"></div>
                          <span className="flex-shrink mx-3 text-xs text-muted-foreground">hoặc</span>
                          <div className="flex-grow border-t border-input"></div>
                        </div>

                        <button
                          type="button"
                          onClick={handleGoogleLogin}
                          className="w-full flex items-center justify-center gap-3 border border-input py-2 px-4 rounded hover:bg-muted transition-colors"
                        >
                          <svg className="w-5 h-5" viewBox="0 0 24 24">
                            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                          </svg>
                          {mode === "login" ? "Đăng nhập với Google" : "Đăng ký với Google"}
                        </button>
                        {/* ============================= */}

                        <div className="mt-6 text-center text-sm text-muted-foreground">
                          <div>Quên mật khẩu?{" "}
                            <Link href="/forgot-password" className="text-blue-500 dark:text-blue-400 hover:underline">Tạo mật khẩu mới</Link>
                          </div>
                        </div>
                      </form>
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