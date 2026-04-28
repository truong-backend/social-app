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
const FormFields = ({ mode, formData, setFormData, showPassword, setShowPassword, loading, verifying, showConfirmPassword, setShowConfirmPassword, showResendButton, onResend }) => {
  const handleInputChange = (field) => (e) => setFormData(prev => ({ ...prev, [field]: e.target.value }));
  const isDisabled = loading || verifying;

  return (
    <>
      {/* Email */}
      <div className="space-y-2 mb-2">
        <h4 className="text-sm font-medium text-muted-foreground">Email</h4>
        <input
          type="email" value={formData.email} onChange={handleInputChange("email")}
          className="w-full bg-transparent border-b border-input px-0 py-1 focus:outline-none focus:border-primary text-foreground"
          required disabled={isDisabled}
        />
      </div>

      {/* Register fields */}
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

      {/* Password */}
      <div className="space-y-2 relative">
        <h4 className="text-sm font-medium text-muted-foreground">Mật khẩu</h4>
        <input
          type={showPassword ? "text" : "password"} value={formData.password} onChange={handleInputChange("password")}
          className="w-full bg-transparent border-b border-input px-0 py-1 focus:outline-none focus:border-primary pr-10 text-foreground"
          required minLength={8} disabled={isDisabled}
        />
        <p className="text-gray-500 text-sm">
          Mật khẩu phải có tối thiểu 8 kí tự, bao gồm ít nhất 1 chữ cái thường, 1 chữ cái hoa, 1 chữ số và kí tự đặc biệt @ $ ! % * ? &
        </p>
        <button type="button" className="absolute right-0 top-7 p-1 text-muted-foreground hover:text-foreground"
          onClick={() => setShowPassword(prev => !prev)} tabIndex={-1} disabled={isDisabled}>
          {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
        </button>
      </div>

      {/* Confirm password */}
      {mode === "register" && (
          <div className="space-y-2 relative">
            <h4 className="text-sm font-medium text-muted-foreground">Nhập lại mật khẩu</h4>
            <input
                type={showConfirmPassword ? "text" : "password"}
                value={formData.confirmPassword}
                onChange={handleInputChange("confirmPassword")}
                className="w-full bg-transparent border-b border-input px-0 py-1 focus:outline-none focus:border-primary pr-10 text-foreground"
                required minLength={6} disabled={loading}
            />
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
// Google Login Button Component
// ============================================================
const GoogleLoginButton = ({ onSuccess, onError, loading }) => {
  const buttonRef = useRef(null);

  useEffect(() => {
    const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
    if (!clientId) {
      console.warn("NEXT_PUBLIC_GOOGLE_CLIENT_ID chưa được cấu hình");
      return;
    }

    // Load Google Identity Services script
    const loadGSI = () => {
      if (window.google?.accounts) {
        initializeGSI();
        return;
      }
      const script = document.createElement("script");
      script.src = "https://accounts.google.com/gsi/client";
      script.async = true;
      script.defer = true;
      script.onload = initializeGSI;
      document.head.appendChild(script);
    };

    const initializeGSI = () => {
      if (!window.google?.accounts?.id) return;
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: (response) => {
          if (response.credential) {
            onSuccess(response.credential);
          } else {
            onError("Không nhận được credential từ Google");
          }
        },
        auto_select: false,
        cancel_on_tap_outside: true,
      });

      if (buttonRef.current) {
        window.google.accounts.id.renderButton(buttonRef.current, {
          type: "standard",
          theme: "outline",
          size: "large",
          text: "signin_with",
          shape: "rectangular",
          logo_alignment: "left",
          width: buttonRef.current.offsetWidth || 320,
        });
      }
    };

    loadGSI();
  }, [onSuccess, onError]);

  return (
    <div className="w-full flex justify-center">
      <div
        ref={buttonRef}
        className="w-full"
        style={{ minHeight: 44, opacity: loading ? 0.6 : 1, pointerEvents: loading ? "none" : "auto" }}
      />
    </div>
  );
};

// Loading component
const AuthPageLoading = () => (
  <div className="min-h-screen bg-background text-foreground flex flex-col">
    <main className="flex-grow flex flex-col md:flex-row h-full">
      <div className="flex items-center justify-center flex-1">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    </main>
  </div>
);

// ============================================================
// Main Auth Form
// ============================================================
function AuthFormContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [mode, setMode] = useState("login");
  const [formData, setFormData] = useState({
    email: "", password: "", confirmPassword: "",
    givenName: "", familyName: "", birthdate: "",
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [verifyMessage, setVerifyMessage] = useState("");
  const [verifying, setVerifying] = useState(false);
  const [showResendButton, setShowResendButton] = useState(false);
  const [ref, { height }] = useMeasure();

  // Xử lý verify email từ URL
  useEffect(() => {
    const token = searchParams.get("token");
    const verifyEmail = searchParams.get("verifyEmail");

    if (token && verifyEmail) {
      setVerifying(true);
      api.post(`/v1/register/verify`, { token, email: verifyEmail })
        .then(() => setVerifyMessage("✅ Email đã được xác thực thành công! Bạn có thể đăng nhập ngay."))
        .catch((error) => setVerifyMessage(`❌ ${parseApiError(error)}`))
        .finally(() => setVerifying(false));
    }
  }, [searchParams]);

  const handleSubmit = useCallback(async (e) => {
    e.preventDefault();
    setMessage("");

    const validationError = validateForm(mode, formData);
    if (validationError) { setMessage(validationError); return; }

    setLoading(true);
    try {
      if (mode === "login") {
        const { data } = await api.post("/v1/auth/login", {
          email: formData.email,
          password: formData.password,
        });
        const token = data.body?.token;
        if (!token) throw new Error("Không nhận được token");

        const decoded = jwtDecode(token);
        const userId = decoded.sub;
        const username = decoded.username;

        setAuthToken(token, userId, username);
        router.push("/home");
      } else {
        await api.post("/v1/register", {
          email: formData.email,
          password: formData.password,
          givenName: formData.givenName,
          familyName: formData.familyName,
          birthdate: formData.birthdate,
        });
        setMessage("✅ Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
        setShowResendButton(true);
      }
    } catch (error) {
      const errMsg = parseApiError(error);
      const data = error.response?.data;
      if (data?.body?.time) {
        setMessage(`🔒 Tài khoản bị khóa đến ${formatLockoutTime(data.body.time)}`);
      } else if (data?.body?.remainingAttempts) {
        setMessage(`❌ ${errMsg}. Còn ${data.body.remainingAttempts} lần thử.`);
      } else {
        setMessage(`❌ ${errMsg}`);
      }
    } finally {
      setLoading(false);
    }
  }, [mode, formData, router]);

  // ============================================================
  // Google OAuth Handler
  // ============================================================
  const handleGoogleSuccess = useCallback(async (idToken) => {
    setGoogleLoading(true);
    setMessage("");
    try {
      const { data } = await api.post("/v1/auth/google", { idToken });
      const token = data.body?.token;
      if (!token) throw new Error("Không nhận được token từ server");

      const decoded = jwtDecode(token);
      const userId = decoded.sub;
      const username = decoded.username;

      setAuthToken(token, userId, username);
      router.push("/home");
    } catch (error) {
      setMessage(`❌ Đăng nhập Google thất bại: ${parseApiError(error)}`);
    } finally {
      setGoogleLoading(false);
    }
  }, [router]);

  const handleGoogleError = useCallback((errorMsg) => {
    setMessage(`❌ Lỗi Google: ${errorMsg}`);
  }, []);

  const handleResend = async () => {
    if (!formData.email) { setMessage("❌ Vui lòng nhập email"); return; }
    try {
      await api.post("/v1/register/resend-verify", { email: formData.email });
      setMessage("✅ Đã gửi lại email xác thực!");
    } catch (error) {
      setMessage(`❌ ${parseApiError(error)}`);
    }
  };

  const toggleMode = () => {
    setMode(prev => prev === "login" ? "register" : "login");
    setMessage("");
    setVerifyMessage("");
    setShowResendButton(false);
  };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col">
      <main className="flex-grow flex flex-col md:flex-row h-full">
        {/* Left Side - Banner */}
        <div className="hidden md:flex flex-col justify-center items-start bg-primary text-primary-foreground p-12 md:w-1/2 lg:w-3/5">
          <div className="max-w-md">
            <div className="flex items-center gap-3 mb-8">
              <Image src="/pocpoc.png" alt="PocPoc Logo" width={48} height={48} className="rounded-xl" />
              <h1 className="text-3xl font-bold">PocPoc</h1>
            </div>
            <h2 className="text-4xl font-bold leading-tight mb-4">
              Kết nối với bạn bè và thế giới xung quanh bạn
            </h2>
            <p className="text-lg opacity-80">
              Chia sẻ câu chuyện, gặp gỡ bạn mới và luôn được là chính mình trên PocPoc.
            </p>
          </div>
        </div>

        {/* Right Side - Form */}
        <div className="flex flex-col justify-center items-center p-6 md:p-12 md:w-1/2 lg:w-2/5">
          <div className="w-full max-w-sm">
            {/* Mobile Logo */}
            <div className="flex md:hidden items-center gap-2 mb-8">
              <Image src="/pocpoc.png" alt="PocPoc Logo" width={36} height={36} className="rounded-lg" />
              <span className="text-xl font-bold">PocPoc</span>
            </div>

            <motion.div animate={{ height }} className="overflow-hidden">
              <div ref={ref}>
                <AnimatePresence mode="wait">
                  <MotionContainer key={mode} modeKey={mode} effect="fadeUp" duration={0.2}>
                    <div className="space-y-4">
                      <div>
                        <h2 className="text-2xl font-bold text-foreground">
                          {mode === "login" ? "Đăng nhập" : "Tạo tài khoản"}
                        </h2>
                        <p className="text-sm text-muted-foreground mt-1">
                          {mode === "login"
                            ? "Chào mừng trở lại! Vui lòng đăng nhập để tiếp tục."
                            : "Tạo tài khoản miễn phí để bắt đầu kết nối!"}
                        </p>
                      </div>

                      <MessageDisplay
                        message={message}
                        verifyMessage={verifyMessage}
                        verifying={verifying}
                      />

                      {/* ===== GOOGLE LOGIN BUTTON ===== */}
                      <div className="space-y-3">
                        <GoogleLoginButton
                          onSuccess={handleGoogleSuccess}
                          onError={handleGoogleError}
                          loading={googleLoading || loading}
                        />

                        {googleLoading && (
                          <p className="text-center text-sm text-muted-foreground animate-pulse">
                            🔄 Đang xác thực với Google...
                          </p>
                        )}

                        {/* Divider */}
                        <div className="flex items-center gap-3">
                          <div className="flex-1 border-t border-border" />
                          <span className="text-xs text-muted-foreground">hoặc</span>
                          <div className="flex-1 border-t border-border" />
                        </div>
                      </div>
                      {/* ===== END GOOGLE LOGIN BUTTON ===== */}

                      <form onSubmit={handleSubmit} className="space-y-4">
                        <FormFields
                          mode={mode}
                          formData={formData}
                          setFormData={setFormData}
                          showPassword={showPassword}
                          setShowPassword={setShowPassword}
                          loading={loading}
                          verifying={verifying}
                          showConfirmPassword={showConfirmPassword}
                          setShowConfirmPassword={setShowConfirmPassword}
                          showResendButton={showResendButton}
                          onResend={handleResend}
                        />

                        {showResendButton && (
                          <button
                            type="button"
                            onClick={handleResend}
                            className="text-sm text-primary hover:underline"
                          >
                            Gửi lại email xác thực
                          </button>
                        )}

                        {mode === "login" && (
                          <div className="text-right">
                            <Link href="/forgot-password" className="text-sm text-primary hover:underline">
                              Quên mật khẩu?
                            </Link>
                          </div>
                        )}

                        <Button
                          type="submit"
                          className="w-full"
                          disabled={loading || verifying || googleLoading}
                        >
                          {loading
                            ? "⏳ Đang xử lý..."
                            : mode === "login" ? "Đăng nhập" : "Đăng ký"}
                        </Button>
                      </form>

                      <div className="text-center">
                        <button
                          type="button"
                          onClick={toggleMode}
                          className="text-sm text-primary hover:underline flex items-center gap-1 mx-auto"
                          disabled={loading || googleLoading}
                        >
                          <ArrowLeftRight className="w-3 h-3" />
                          {mode === "login"
                            ? "Chưa có tài khoản? Đăng ký ngay"
                            : "Đã có tài khoản? Đăng nhập"}
                        </button>
                      </div>
                    </div>
                  </MotionContainer>
                </AnimatePresence>
              </div>
            </motion.div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default function RegisterPage() {
  return (
    <Suspense fallback={<AuthPageLoading />}>
      <AuthFormContent />
    </Suspense>
  );
}