package com.app.figmaai.backend.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CopyingRequestWrapper extends ContentCachingRequestWrapper {

  private boolean inputStreamRead = false;

  public CopyingRequestWrapper(final HttpServletRequest request) {
    super(request);
  }

  @NotNull
  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (!inputStreamRead) {
      inputStreamRead = true;
      return super.getInputStream();
    }
    return new BodyInputStream(getContentAsByteArray());
  }

  static class BodyInputStream extends ServletInputStream {
    private final InputStream delegate;

    public BodyInputStream(byte[] body) {
      this.delegate = new ByteArrayInputStream(body);
    }

    public boolean isFinished() {
      try {
        return this.delegate.available() == 0;
      } catch (IOException e) {
        return false;
      }
    }

    public boolean isReady() {
      return true;
    }

    public void setReadListener(ReadListener readListener) {
      throw new UnsupportedOperationException();
    }

    public int read() throws IOException {
      return this.delegate.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
      return this.delegate.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
      return this.delegate.read(b);
    }

    public long skip(long n) throws IOException {
      return this.delegate.skip(n);
    }

    public int available() throws IOException {
      return this.delegate.available();
    }

    public void close() throws IOException {
      this.delegate.close();
    }

    public synchronized void mark(int readlimit) {
      this.delegate.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
      this.delegate.reset();
    }

    public boolean markSupported() {
      return this.delegate.markSupported();
    }
  }
}
