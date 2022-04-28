package io.wcm.renderfarm.api;

/**
 * Runtime Exception when something goes wrong
 */
public class RenderRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs an instance of <code>RenderRuntimeException</code> with the
   * specified detail message.
   *
   * @param msg the detail message.
   */
  public RenderRuntimeException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of <code>RenderRuntimeException</code> with the
   * specified detail message and cause.
   *
   * @param msg the detail message.
   * @param cause the cause
   */
  public RenderRuntimeException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
