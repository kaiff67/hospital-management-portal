package com.hospital.util;
import com.hospital.model.*;
import java.util.Set;
public final class Access {
  private Access() { }
  public static void require(User user, Role... allowed) {
    if(user==null || !user.active() || !Set.of(allowed).contains(user.role())) throw new SecurityException("You are not authorized to perform this operation.");
  }
}
