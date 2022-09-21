package com.app.drivn.backend.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class LogUtil {
  private LogUtil() {
  }

  public static String getStacktraceTillSource(final Throwable throwable) {
    final StringTokenizer frames = getFrames(throwable);

    final StringJoiner result = new StringJoiner(System.lineSeparator());

    while (frames.hasMoreTokens()) {
      final String frame = frames.nextToken();
      result.add(frame);
      if (frame.startsWith("\tat com.app.drivn")) {
        break;
      }
    }

    return result.toString();

  }

  public static String getStacktraceLast(final Throwable throwable, final int count) {
    final StringTokenizer frames = getFrames(throwable);

    final StringJoiner result = new StringJoiner(System.lineSeparator());

    for (int i = 0; i < count && frames.hasMoreTokens(); i++) {
      result.add(frames.nextToken());
    }

    return result.toString();
  }

  private static StringTokenizer getFrames(final Throwable throwable) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw, true);
    throwable.printStackTrace(pw);

    return new StringTokenizer(
        sw.getBuffer().toString(),
        System.lineSeparator()
    );
  }

  public static String getStacktraceLast(final Throwable throwable) {
    return Arrays.stream(throwable.getStackTrace())
        .map(StackTraceElement::toString)
        .collect(Collectors.joining(System.lineSeparator()));
  }
}
