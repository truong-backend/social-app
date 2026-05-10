"use client";

import { useTheme } from "next-themes";
import { useEffect, useRef, useState } from "react";
import { AnimatePresence } from "framer-motion";
import { usePathname } from "next/navigation";
import MotionContainer from "@/components/ui-components/MotionContainer";
import Header from "@/components/ui-components/Header";
import Sidebar from "@/components/ui-components/Sidebar";
import ProgressBar from "@/components/ui-components/ProgressBar";
import { Toaster } from "react-hot-toast";
import useNotificationSocket from "@/hooks/useNotificationSocket";
import useMessageNotification from "@/hooks/useMessageNotification";
import useErrorSocket from "@/hooks/useErrorSocket";
import useOnlineNotification from "@/hooks/useOnlineNotification";
import { getAuthInfo } from "@/utils/axios";
import { CallProvider } from "@/context/CallContext";
import { useCall } from "@/context/CallContext";
import CallPopup from "@/components/social-app-component/CallPopup";
import CallVideo from "@/components/social-app-component/CallVideo";
import ThemeProvider from "@/providers/ThemeProvider";
import { useRouter } from "next/navigation";
import { pageMetadata, usePageMetadata } from "@/utils/clientMetadata";
import RightSidebar from "@/components/social-app-component/RightSidebar";
import ChatList from "@/components/social-app-component/ChatList";
import Chatbox from "@/components/social-app-component/ChatBox";

function GlobalCallInterface() {
  const {
    incomingCaller,
    currentCall,
    localStream,
    remoteStream,
    isCallEnding,
    acceptCall,
    rejectCall,
  } = useCall();
  const router = useRouter();
  const [showCallVideo, setShowCallVideo] = useState(false);

  useEffect(() => {
    const authInfo = getAuthInfo();
    if (!authInfo) {
      router.push("/register");
      return;
    }
    if (!authInfo.token || !authInfo.userId || !authInfo.userName) {
      router.push("/register");
    }
  }, [router]);

  useEffect(() => {
    setShowCallVideo(currentCall || isCallEnding);
  }, [currentCall, isCallEnding]);

  return (
    <>
      {incomingCaller && !currentCall && !isCallEnding && (
        <CallPopup caller={incomingCaller} onAccept={acceptCall} onReject={rejectCall} />
      )}
      {showCallVideo && (
        <CallVideo
          localStream={localStream}
          remoteStream={remoteStream}
          onCallEnd={() => setShowCallVideo(false)}
        />
      )}
    </>
  );
}

function shouldShowHeader(pathname) {
  if (pathname.startsWith("/settings")) return false;
  if (["/", "/home", "/search", "/friends"].includes(pathname)) return true;
  if (pathname.startsWith("/profile/")) return true;
  return false;
}

function LeftSidebar({ narrow }) {
  const logoStyle = { fontFamily: "'Billabong','Grand Hotel',cursive,Arial,sans-serif" };
  const wideClass = narrow ? "w-16" : "w-16 lg:w-56 xl:w-64";
  return (
    <aside
      className={
        "hidden md:flex flex-col flex-shrink-0 h-screen sticky top-0 z-40 bg-[var(--background)] border-r border-[var(--border)] transition-all duration-200 " +
        wideClass
      }
    >
      <div className="px-3 py-5 mb-1">
        <a
          href="/home"
          className="font-bold text-xl italic hidden lg:block text-[var(--foreground)]"
          style={logoStyle}
        >
          pocpoc
        </a>
        <a href="/home" className="flex items-center justify-center lg:hidden">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.5"
            className="w-7 h-7 text-[var(--foreground)]"
          >
            <rect x="2" y="2" width="20" height="20" rx="5" ry="5" />
            <circle cx="12" cy="12" r="4" />
            <circle cx="17.5" cy="6.5" r="1.2" fill="currentColor" stroke="none" />
          </svg>
        </a>
      </div>
      <div className="flex-1 overflow-y-auto">
        <Sidebar />
      </div>
    </aside>
  );
}

function MainLayoutContent({ children }) {
  const { resolvedTheme } = useTheme();
  const pathname = usePathname();
  const [mounted, setMounted] = useState(false);
  const prevThemeRef = useRef(null);
  const [userId, setUserId] = useState(null);
  const [token, setToken] = useState(null);
  const [activeChatId, setActiveChatId] = useState(null);
  const [activeTargetUser, setActiveTargetUser] = useState(null);

  const { initializeCall, currentCall, isCallEnding } = useCall();
  const showHeader = shouldShowHeader(pathname);
  const isInCall = currentCall || isCallEnding;

  const hideRightSidebar =
    pathname.startsWith("/settings") ||
    pathname.startsWith("/search") ||
    pathname.startsWith("/chats");

  const isHomePage = pathname === "/home" || pathname === "/";

  useEffect(() => {
    const storedUserId = localStorage.getItem("userId");
    const storedToken = localStorage.getItem("accessToken");
    if (storedUserId && storedToken) {
      setUserId(storedUserId);
      setToken(storedToken);
      initializeCall(storedToken);
    }
  }, [initializeCall]);

  useMessageNotification(userId);
  useNotificationSocket(userId, token);
  useOnlineNotification(userId);
  useErrorSocket(userId);

  useEffect(() => {
    const handleNewMessage = (event) => {
      const messageData = event.detail;
      if (pathname === "/chats" && !activeChatId) {
        setActiveChatId(messageData.chatId);
        setActiveTargetUser(messageData.sender);
      }
    };
    const handleOpenChat = (event) => {
      const { chatId, targetUser } = event.detail;
      setActiveChatId(chatId);
      setActiveTargetUser(targetUser);
    };
    const handleErrorReceived = (event) => {
      console.log("Error received:", event.detail);
    };
    window.addEventListener("newMessageReceived", handleNewMessage);
    window.addEventListener("openChat", handleOpenChat);
    window.addEventListener("errorReceived", handleErrorReceived);
    return () => {
      window.removeEventListener("newMessageReceived", handleNewMessage);
      window.removeEventListener("openChat", handleOpenChat);
      window.removeEventListener("errorReceived", handleErrorReceived);
    };
  }, [pathname, activeChatId]);

  useEffect(() => {
    setMounted(true);
  }, []);

  const shouldAnimate =
    mounted && prevThemeRef.current && prevThemeRef.current !== resolvedTheme;

  useEffect(() => {
    if (mounted) prevThemeRef.current = resolvedTheme;
  }, [resolvedTheme, mounted]);

  const feedMaxWidth = hideRightSidebar ? "max-w-3xl" : "max-w-xl";
  const mainPaddingTop = showHeader ? "60px" : "0";

  const renderRightSidebar = () => {
    if (hideRightSidebar) return null;
    return (
      <aside className="hidden lg:flex flex-col w-80 xl:w-96 flex-shrink-0 h-screen sticky top-0 overflow-y-auto">
        {isHomePage ? (
          <RightSidebar token={token} />
        ) : (
          <div className="flex flex-col w-full h-full pt-4 pr-4 gap-4">
            {activeChatId && activeTargetUser ? (
              <Chatbox
                chatId={activeChatId}
                targetUser={activeTargetUser}
                onBack={() => {
                  setActiveChatId(null);
                  setActiveTargetUser(null);
                }}
                onChatCreated={(id, user) => {
                  setActiveChatId(id);
                  setActiveTargetUser(user);
                }}
                beToken={token}
                recipientId={activeTargetUser?.id || activeTargetUser?.userId}
              />
            ) : (
              <ChatList
                onSelectChat={(chatId, user) => {
                  setActiveChatId(chatId);
                  setActiveTargetUser(user);
                }}
                selectedChatId={activeChatId}
              />
            )}
          </div>
        )}
      </aside>
    );
  };

  const layoutContent = (
    <>
      <ProgressBar />
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: "var(--background)",
            color: "var(--foreground)",
            border: "1px solid var(--border)",
            borderRadius: "12px",
          },
        }}
      />

      {!isInCall && (
        <div className="h-screen flex flex-col">
          {showHeader && (
            <header className="md:hidden fixed top-0 left-0 right-0 z-50 bg-[var(--background)] border-b border-[var(--border)]">
              <Header />
            </header>
          )}

          <div
            className="flex flex-1 bg-[var(--background)] text-[var(--foreground)] transition-colors duration-500 overflow-hidden"
            style={{ height: "100vh" }}
          >
            <LeftSidebar narrow={hideRightSidebar} />

            <main
              className="flex-1 h-screen overflow-y-auto"
              style={{ paddingTop: pathname === "/chats" ? "0" : mainPaddingTop }}
            >
              <div className={pathname === "/chats" ? "w-full h-full" : pathname.startsWith("/settings") ? "w-full h-full" : "w-full mx-auto pb-16 md:pb-8 " + feedMaxWidth}>
                {showHeader && (
                  <div className="hidden md:block sticky top-0 z-30 bg-[var(--background)] border-b border-[var(--border)]">
                    <Header />
                  </div>
                )}
                {children}
              </div>
            </main>

            {renderRightSidebar()}
          </div>

          <div className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-[var(--background)] border-t border-[var(--border)] h-14">
            <Sidebar />
          </div>
        </div>
      )}

      {isInCall && (
        <div className="h-screen flex flex-col">
          <div
            className="flex flex-1 bg-[var(--background)] text-[var(--foreground)] transition-colors duration-500 overflow-hidden"
            style={{ height: "100vh" }}
          >
            <LeftSidebar narrow={true} />
            <main className="flex-1 h-screen overflow-y-auto">
              <div className={"w-full mx-auto pb-16 md:pb-8 " + feedMaxWidth}>
                {children}
              </div>
            </main>
          </div>
        </div>
      )}

      <GlobalCallInterface />
    </>
  );

  return shouldAnimate ? (
    <AnimatePresence mode="wait">
      <MotionContainer
        key={resolvedTheme}
        modeKey={resolvedTheme}
        effect="fadeUp"
        duration={0.25}
      >
        {layoutContent}
      </MotionContainer>
    </AnimatePresence>
  ) : (
    layoutContent
  );
}

export default function MainLayout({ children }) {
  usePageMetadata(pageMetadata.home());
  return (
    <CallProvider>
      <ThemeProvider>
        <MainLayoutContent>{children}</MainLayoutContent>
      </ThemeProvider>
    </CallProvider>
  );
}