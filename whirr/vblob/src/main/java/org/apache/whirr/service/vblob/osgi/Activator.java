package org.apache.whirr.service.vblob.osgi;

import org.apache.whirr.service.ClusterActionHandler;
import org.apache.whirr.service.vblob.VBlobHandler;
import org.jclouds.scriptbuilder.functionloader.osgi.BundleFunctionLoader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Properties;

public class Activator implements BundleActivator {

  private BundleFunctionLoader functionLoader;
  private final ClusterActionHandler clusterActionHandler = new VBlobHandler();
  private ServiceRegistration registration;

  /**
   * Called when this bundle is started so the Framework can perform the
   * bundle-specific activities necessary to start this bundle. This method
   * can be used to register services or to allocate any resources that this
   * bundle needs.
   * <p/>
   * <p/>
   * This method must complete and return to its caller in a timely manner.
   *
   * @param context The execution context of the bundle being started.
   * @throws Exception If this method throws an exception, this
   *                   bundle is marked as stopped and the Framework will remove this
   *                   bundle's listeners, unregister all services registered by this
   *                   bundle, and release all services used by this bundle.
   */
  @Override
  public void start(BundleContext context) throws Exception {
    //Initialize OSGi based FunctionLoader
    functionLoader = new BundleFunctionLoader(context);
    functionLoader.start();

    Properties props = new Properties();
    props.put("name", "vblob");
    registration = context.registerService(ClusterActionHandler.class.getName(), clusterActionHandler, props);
  }

  /**
   * Called when this bundle is stopped so the Framework can perform the
   * bundle-specific activities necessary to stop the bundle. In general, this
   * method should undo the work that the <code>BundleActivator.start</code>
   * method started. There should be no active threads that were started by
   * this bundle when this bundle returns. A stopped bundle must not call any
   * Framework objects.
   * <p/>
   * <p/>
   * This method must complete and return to its caller in a timely manner.
   *
   * @param context The execution context of the bundle being stopped.
   * @throws Exception If this method throws an exception, the
   *                   bundle is still marked as stopped, and the Framework will remove
   *                   the bundle's listeners, unregister all services registered by the
   *                   bundle, and release all services used by the bundle.
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    if (registration != null) {
      registration.unregister();
    }
  }
}

