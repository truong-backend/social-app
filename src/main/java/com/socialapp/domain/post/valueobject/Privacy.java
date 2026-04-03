package com.socialapp.domain.post.valueobject;

public enum Privacy {
    PUBLIC,
    FRIENDS,
    PRIVATE;

    public boolean isVisibleTo(boolean isFriend, boolean isOwner) {
        return switch (this) {
            case PUBLIC  -> true;
            case FRIENDS -> isOwner || isFriend;
            case PRIVATE -> isOwner;
        };
    }
}