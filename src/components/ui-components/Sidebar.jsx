"use client";
import { useEffect, useState, useRef } from "react";
import { createPortal } from "react-dom";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  Home,
  Search,
  MessageCircle,
  Users,
  UserPen,
  Settings,
  LogOut,
  Menu,
  Bell,
  Compass,
  Film,
  Sun,
  Moon,
  Monitor,
} from "lucide-react";
import { useTheme } from "next-themes";
import Badge from "@/components/ui-components/Badge";
import api, { clearSession, getUserName } from "@/utils/axios";
import NotificationList from "../social-app-component/NotificationList";
import useAppStore from "@/store/ZustandStore";

export default function SidebarNavigation() {
  const pathname = usePathname();
  const router = useRouter();
  const { theme, setTheme, resolvedTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  const [username, setUsername] = useState(null);
  const [showSettingsDropdown, setShowSettingsDropdown] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [dropdownPosition, setDropdownPosition] = useState({ top: 0, left: 0 });
  const [showNotifications, setShowNotifications] = useState(false);
  const [badgeCount, setBadgeCount] = useState(0);
  const [isMarkingAsRead, setIsMarkingAsRead] = useState(false);
  const [notificationPosition, setNotificationPosition] = useState({
    top: 10,
    left: 0,
  });

  const dropdownRef = useRef(null);
  const moreButtonRef = useRef(null);
  const notificationRef = useRef(null);
  const notificationButtonRef = useRef(null);

  const clearAllData = useAppStore((state) => state.clearAllData);
  const unreadNotificationCount = useAppStore(
    (state) => state.unreadNotificationCount,
  );
  const unreadNotificationCountFromSocket = useAppStore(
    (state) => state.unreadNotificationCountFromSocket,
  );
  const fetchNotifications = useAppStore((state) => state.fetchNotifications);
  const unreadMessageCount = useAppStore((state) => state.unreadMessageCount);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    setBadgeCount(unreadNotificationCount + unreadNotificationCountFromSocket);
  }, [unreadNotificationCount, unreadNotificationCountFromSocket]);

  useEffect(() => {
    const storedUsername = getUserName();
    if (storedUsername) setUsername(storedUsername);
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowSettingsDropdown(false);
      }
      if (
        notificationRef.current &&
        !notificationRef.current.contains(event.target)
      ) {
        setShowNotifications(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleNotificationClick = async () => {
    if (showNotifications) {
      setShowNotifications(false);
      return;
    }
    if (notificationButtonRef.current) {
      const isDesktop = window.innerWidth >= 768;
      if (isDesktop) {
        setNotificationPosition({ top: 64, left: 96 });
      } else {
        setNotificationPosition({ top: 0, left: 0 });
      }
    }
    setIsMarkingAsRead(true);
    try {
      if (unreadNotificationCountFromSocket > 0) {
        await api.patch(
          "/v1/notifications/mark-as-read?limit=" +
            unreadNotificationCountFromSocket,
        );
      }
      await fetchNotifications(true);
      setShowNotifications(true);
      setBadgeCount(0);
    } catch (error) {
      setShowNotifications(true);
      setBadgeCount(0);
    } finally {
      setIsMarkingAsRead(false);
    }
  };

  const handleLogout = async () => {
    if (isLoggingOut) return;
    setIsLoggingOut(true);
    try {
      await api.delete("/v1/auth/logout");
    } catch (err) {
      console.error("Logout failed:", err.response?.data || err.message);
    } finally {
      clearSession();
      clearAllData();
      router.replace("/register");
      setIsLoggingOut(false);
    }
  };

  const handleMoreClick = () => {
    if (!showSettingsDropdown && moreButtonRef.current) {
      const rect = moreButtonRef.current.getBoundingClientRect();
      const isDesktop = window.innerWidth >= 768;
      if (isDesktop) {
        // Hiện ra bên PHẢI của sidebar, không che nội dung
        setDropdownPosition({
          top: rect.bottom - 220,
          left: rect.right + 12,
        });
      } else {
        // Mobile: hiện lên trên bottom nav
        setDropdownPosition({
          top: "auto",
          bottom: 64,
          left: 8,
        });
      }
    }
    setShowSettingsDropdown(!showSettingsDropdown);
  };

  const themeOptions = [
    { key: "light", label: "Sáng", icon: <Sun size={14} /> },
    { key: "dark", label: "Tối", icon: <Moon size={14} /> },
    { key: "system", label: "Hệ thống", icon: <Monitor size={14} /> },
  ];

  const renderDropdown = () => {
    if (!showSettingsDropdown) return null;
    const posStyle = dropdownPosition.bottom
      ? {
          bottom: dropdownPosition.bottom + "px",
          left: dropdownPosition.left + "px",
        }
      : {
          top: dropdownPosition.top + "px",
          left: dropdownPosition.left + "px",
        };
    return createPortal(
      <div
        ref={dropdownRef}
        className="fixed bg-white dark:bg-[#262626] rounded-2xl shadow-2xl border border-gray-200 dark:border-[#3a3a3a] py-2 z-[9999]"
        style={{ ...posStyle, minWidth: "240px" }}
      >
        {/* Theme */}
        <div className="px-4 py-3 border-b border-gray-100 dark:border-[#3a3a3a]">
          <p className="text-[11px] text-gray-400 dark:text-gray-500 mb-2.5 font-semibold uppercase tracking-wider">
            Giao diện
          </p>
          <div className="flex gap-1.5">
            {themeOptions.map((opt) => {
              const isActive = theme === opt.key;
              return (
                <button
                  key={opt.key}
                  onClick={() => setTheme(opt.key)}
                  className={
                    "flex-1 flex flex-col items-center gap-1.5 py-2.5 px-1 rounded-xl text-[11px] font-semibold border transition-all " +
                    (isActive
                      ? "bg-gray-900 dark:bg-white text-white dark:text-gray-900 border-transparent"
                      : "text-gray-500 dark:text-gray-400 border-gray-200 dark:border-[#3a3a3a] hover:bg-gray-50 dark:hover:bg-[#363636]")
                  }
                >
                  {opt.icon}
                  <span>{opt.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        <Link
          href="/settings/personalinfo"
          onClick={() => setShowSettingsDropdown(false)}
          className="flex items-center px-4 py-3 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-[#363636] transition-colors gap-3"
        >
          <Settings size={18} />
          <span>Cài đặt</span>
        </Link>

        <div className="border-t border-gray-100 dark:border-[#3a3a3a] my-1" />

        <button
          onClick={() => {
            handleLogout();
            setShowSettingsDropdown(false);
          }}
          disabled={isLoggingOut}
          className="w-full flex items-center px-4 py-3 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-[#363636] transition-colors gap-3 disabled:opacity-50"
        >
          <LogOut size={18} />
          <span>{isLoggingOut ? "Đang đăng xuất..." : "Đăng xuất"}</span>
        </button>
      </div>,
      document.body,
    );
  };

  const renderNotifications = () => {
    if (!showNotifications) return null;
    const isMobile = typeof window !== "undefined" && window.innerWidth < 768;
    const notifStyle = isMobile
      ? { bottom: "80px", top: "auto", left: "0", right: "0" }
      : {
          top: notificationPosition.top + "px",
          left: notificationPosition.left + "px",
        };
    return createPortal(
      <div
        ref={notificationRef}
        className="fixed z-[9999] overflow-y-auto rounded-2xl shadow-2xl bg-[var(--card)] border border-[var(--border)] md:w-80 md:max-h-[calc(100vh-80px)] w-full max-h-[60vh] left-0 right-0 md:left-auto md:right-auto"
        style={notifStyle}
      >
        <NotificationList />
      </div>,
      document.body,
    );
  };

  // Safe theme icon after mount
  const themeIcon = mounted ? (
    resolvedTheme === "dark" ? (
      <Sun size={26} strokeWidth={1.5} />
    ) : (
      <Moon size={26} strokeWidth={1.5} />
    )
  ) : (
    <Moon size={26} strokeWidth={1.5} />
  );

  const themeLabel = mounted
    ? resolvedTheme === "dark"
      ? "Chế độ sáng"
      : "Chế độ tối"
    : "Đổi giao diện";

  const navItems = [
    {
      href: "/home",
      icon: <Home size={26} strokeWidth={pathname === "/home" ? 2.5 : 1.5} />,
      label: "Trang chủ",
      active: pathname === "/home",
    },
    {
      href: "/search",
      icon: (
        <Search size={26} strokeWidth={pathname === "/search" ? 2.5 : 1.5} />
      ),
      label: "Tìm kiếm",
      active: pathname === "/search",
    },
    {
      href: "/home",
      icon: <Compass size={26} strokeWidth={1.5} />,
      label: "Khám phá",
      active: false,
    },
    {
      href: "/reels",
      icon: <Film size={26} strokeWidth={pathname === "/reels" ? 2.5 : 1.5} />,
      label: "Reels",
      active: pathname === "/reels",
    },
    {
      href: "/chats",
      icon: (
        <span className="relative inline-flex">
          <MessageCircle
            size={26}
            strokeWidth={pathname === "/chats" ? 2.5 : 1.5}
          />
          {unreadMessageCount > 0 && (
            <Badge asNotification>{unreadMessageCount}</Badge>
          )}
        </span>
      ),
      label: "Tin nhắn",
      active: pathname === "/chats",
    },
    {
      href: "/friends",
      icon: (
        <Users size={26} strokeWidth={pathname === "/friends" ? 2.5 : 1.5} />
      ),
      label: "Bạn bè",
      active: pathname === "/friends",
    },
    {
      href: username ? "/profile/" + username : "#",
      icon: (
        <UserPen
          size={26}
          strokeWidth={pathname.startsWith("/profile") ? 2.5 : 1.5}
        />
      ),
      label: "Hồ sơ",
      active: pathname.startsWith("/profile"),
    },
  ];

  const mobileNavItems = [
    {
      href: "/home",
      icon: <Home size={24} strokeWidth={pathname === "/home" ? 2.5 : 1.5} />,
      label: "Home",
    },
    {
      href: "/search",
      icon: (
        <Search size={24} strokeWidth={pathname === "/search" ? 2.5 : 1.5} />
      ),
      label: "Search",
    },
    {
      href: "/chats",
      icon: (
        <span className="relative inline-flex">
          <MessageCircle
            size={24}
            strokeWidth={pathname === "/chats" ? 2.5 : 1.5}
          />
          {unreadMessageCount > 0 && (
            <Badge asNotification>{unreadMessageCount}</Badge>
          )}
        </span>
      ),
      label: "Chats",
    },
    {
      href: "/friends",
      icon: (
        <Users size={24} strokeWidth={pathname === "/friends" ? 2.5 : 1.5} />
      ),
      label: "Friends",
    },
    {
      href: username ? "/profile/" + username : "#",
      icon: (
        <UserPen
          size={24}
          strokeWidth={pathname.startsWith("/profile") ? 2.5 : 1.5}
        />
      ),
      label: "Profile",
    },
  ];

  return (
    <>
      {/* Desktop Sidebar */}
      <div className="hidden md:flex flex-col h-full w-full px-2 py-4 justify-between">
        <nav className="flex flex-col gap-1">
          {navItems.map((item, i) => (
            <Link
              key={i}
              href={item.href}
              className={
                "flex items-center gap-4 px-3 py-3 rounded-xl transition-all duration-150 hover:bg-silver group " +
                (item.active ? "font-bold" : "")
              }
              aria-label={item.label}
              title={item.label}
            >
              <span className="text-[var(--foreground)] flex-shrink-0">
                {item.icon}
              </span>
              <span className="text-sm hidden lg:block text-[var(--foreground)] group-hover:translate-x-0.5 transition-transform">
                {item.label}
              </span>
            </Link>
          ))}

          {/* Notification */}
          <button
            ref={notificationButtonRef}
            type="button"
            aria-label="Thông báo"
            title="Thông báo"
            onClick={handleNotificationClick}
            disabled={isLoggingOut || isMarkingAsRead}
            className={
              "flex items-center gap-4 px-3 py-3 rounded-xl transition-all duration-150 hover:bg-gray-100  w-full group " +
              (showNotifications ? "font-bold" : "") +
              (isLoggingOut || isMarkingAsRead
                ? " opacity-50 cursor-not-allowed"
                : "")
            }
          >
            <span className="relative text-[var(--foreground)] flex-shrink-0">
              {isMarkingAsRead ? (
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-500" />
              ) : (
                <Bell size={26} strokeWidth={showNotifications ? 2.5 : 1.5} />
              )}
              {badgeCount > 0 && !isMarkingAsRead && (
                <Badge asNotification>{badgeCount}</Badge>
              )}
            </span>
            <span className="text-sm hidden lg:block text-[var(--foreground)] group-hover:translate-x-0.5 transition-transform">
              Thông báo
            </span>
          </button>
        </nav>

        {/* Bottom */}
        <div className="flex flex-col gap-1">
          <button
            onClick={() => {
              if (mounted)
                setTheme(resolvedTheme === "dark" ? "light" : "dark");
            }}
            className="flex items-center gap-4 px-3 py-3 rounded-xl transition-all duration-150   w-full group"
            aria-label="Đổi giao diện"
            title={themeLabel}
          >
            <span className="text-[var(--foreground)] flex-shrink-0">
              {themeIcon}
            </span>
            <span className="text-sm hidden lg:block text-[var(--foreground)] group-hover:translate-x-0.5 transition-transform">
              {themeLabel}
            </span>
          </button>

          <button
            ref={moreButtonRef}
            aria-label="Thêm"
            title="Thêm"
            onClick={handleMoreClick}
            className={
              "flex items-center gap-4 px-3 py-3 rounded-xl transition-all duration-150  w-full group " +
              (showSettingsDropdown ? "font-bold" : "")
            }
          >
            <span className="text-[var(--foreground)] flex-shrink-0">
              <Menu size={26} strokeWidth={showSettingsDropdown ? 2.5 : 1.5} />
            </span>
            <span className="text-sm hidden lg:block text-[var(--foreground)] group-hover:translate-x-0.5 transition-transform">
              Thêm
            </span>
          </button>
        </div>
      </div>

      {/* Mobile Bottom Nav */}
      <nav className="md:hidden flex flex-row items-center justify-around w-full h-full px-1">
        {mobileNavItems.map((item, i) => (
          <Link
            key={i}
            href={item.href}
            className="flex items-center justify-center w-11 h-11 rounded-xl transition-colors text-[var(--foreground)]"
            aria-label={item.label}
          >
            {item.icon}
          </Link>
        ))}
        <button
          ref={notificationButtonRef}
          type="button"
          aria-label="Thông báo"
          onClick={handleNotificationClick}
          className="flex items-center justify-center w-11 h-11 rounded-xl transition-colors relative text-[var(--foreground)]"
        >
          <Bell size={24} strokeWidth={1.5} />
          {badgeCount > 0 && <Badge asNotification>{badgeCount}</Badge>}
        </button>
        <button
          onClick={() => {
            if (mounted) setTheme(resolvedTheme === "dark" ? "light" : "dark");
          }}
          className="flex items-center justify-center w-11 h-11 rounded-xl transition-colors  text-[var(--foreground)]"
          aria-label="Đổi giao diện"
        >
          {themeIcon}
        </button>
      </nav>

      {renderDropdown()}
      {renderNotifications()}
    </>
  );
}