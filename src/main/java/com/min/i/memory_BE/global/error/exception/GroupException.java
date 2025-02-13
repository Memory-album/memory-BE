package com.min.i.memory_BE.global.error.exception;

import com.min.i.memory_BE.global.error.ErrorCode;

public class GroupException extends ApiException {
  // 그룹을 찾을 수 없는 경우
  public static class GroupNotFoundException extends GroupException {
    public GroupNotFoundException() {
      super("그룹을 찾을 수 없습니다.", ErrorCode.GROUP_NOT_FOUND);
    }
  }
  
  // 그룹 멤버가 아닌 경우
  public static class NotGroupMemberException extends GroupException {
    public NotGroupMemberException() {
      super("해당 그룹의 멤버가 아닙니다.", ErrorCode.NOT_GROUP_MEMBER);
    }
  }
  
  // 그룹 소유자가 권한 이전 없이 탈퇴를 시도하는 경우
  public static class OwnerCannotLeaveException extends GroupException {
    public OwnerCannotLeaveException() {
      super("그룹 소유자는 먼저 다른 멤버에게 소유권을 이전해야 합니다.", ErrorCode.OWNER_CANNOT_LEAVE);
    }
  }
  
  // 그룹 소유자가 아닌 경우
  public static class NotOwnerException extends GroupException {
    public NotOwnerException() {
      super("그룹 소유자만 이 작업을 수행할 수 있습니다.", ErrorCode.NOT_GROUP_OWNER);
    }
  }
  
  // 그룹 소유자를 삭제하려는 경우
  public static class OwnerCannotBeRemovedException extends GroupException {
    public OwnerCannotBeRemovedException() {
      super("그룹 소유자는 삭제할 수 없습니다.", ErrorCode.OWNER_CANNOT_BE_REMOVED);
    }
  }
  
  public static class GroupMemberNotFoundException extends GroupException {
    public GroupMemberNotFoundException() {
      super("그룹 멤버를 찾을 수 없습니다.", ErrorCode.GROUP_MEMBER_NOT_FOUND);
    }
  }
  
  // 생성자
  protected GroupException(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }
}