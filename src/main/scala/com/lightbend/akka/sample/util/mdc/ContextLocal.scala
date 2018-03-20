//package com.lightbend.akka.sample.util.mdc
//
//import java.util.UUID
//import java.util.concurrent.atomic.AtomicReference
//
//
//final class ContextLocal[T] private(
//                                     // Unique name for the ContextLocal
//                                     private val name: String,
//                                     // Optionally have this ContextLocal proxy something else
//                                     private val proxy: Option[ContextLocal.Proxy[T]] = None
//                                   ) {
//  // Register with the global ThreadLocal state
//  ContextLocal.register(this)
//
//  /**
//    * Set the current value
//    */
//  def set(v: T): Unit = ...
//
//  /**
//    * Get the current value
//    */
//  def get: Option[T] = ...
//
//  /**
//    * Clear the current value
//    */
//  def clear(): Unit = ...
//}
//
//object ContextLocal {
//  trait Proxy[T] {
//    /**
//      * Called when the ContextLocal value is set
//      * (usually as a result of setContext)
//      */
//    def set(v: T): Unit
//
//    /**
//      * Called when the ContextLocal value is retrieved
//      * (usually as a result of getContext)
//      */
//    def get: Option[T]
//
//    /*
//     * Called when the ContextLocal value is cleared
//     * (usually as a result of clearContext)
//     */
//    def clear(): Unit
//  }
//
//  type State = ... // The actual type of this is an implementation detail
//
//  /*
//   * Registration + internal state
//   */
//
//  private[this] val state = new ThreadLocal[State]
//  private[this] val registry = new AtomicReference(Map.empty[String, ContextLocal[_]])
//
//  // This asserts that two ContextLocals do not have the same name, and safely updates the registry
//  private def register(contextLocal: ContextLocal[_]): Unit = ...
//
//  /*
//   * Factory Methods
//   */
//
//  def create[T](name: String = UUID.randomUUID().toString): ContextLocal[T] = new ContextLocal(name)
//  def createProxy[T](name: String = UUID.randomUUID().toString, proxy: Proxy[T]): ContextLocal[T] = new ContextLocal(name, Some(proxy))
//
//  /*
//   * Propagation Methods
//   */
//
//  /**
//    * Get the current state of all ContextLocals
//    */
//  def getContext: State = ...
//
//  /**
//    * Set the current state of all ContextLocals to one retrieved earlier by getContext
//    */
//  def setContext(state: State): Unit = ...
//
//  /**
//    * Clear the value of all the current ContextLocals
//    */
//  def clearContext(): Unit = ...
//
//  /**
//    * Execute a block of code using the specified state of ContextLocals
//    * and restore the current state when complete (success or failure)
//    */
//  def withContext[T](state: State)(f: => T): T = ...
//
//  /**
//    * Execute a block of code with a clear state of ContextLocals
//    * and restore the current state when complete (success or failure)
//    */
//  def withClearContext[T](f: => T): T = ...
//}