package org.junitpioneer.jupiter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

public class SystemPropertyExtension implements BeforeEachCallback, AfterEachCallback {

	private static final String BACKUP_STORE_KEY = "backup";

	@Override
	public void beforeEach( final ExtensionContext context ) throws Exception {
		final Properties backup = new Properties();
		backup.putAll( System.getProperties() );
		final Store store = context.getStore( Namespace.create( getClass(), context.getRequiredTestMethod() ) );
		store.put( BACKUP_STORE_KEY, backup );

		final Method testMethod = context.getTestMethod().get();
		if ( testMethod.isAnnotationPresent( SystemProperty.class ) ) {
			final SystemProperty prop = testMethod.getAnnotation( SystemProperty.class );
			prepareSystemProperty( prop );
		} else {
			final SystemProperties props = testMethod.getAnnotation( SystemProperties.class );
			Arrays.stream( props.value() ).forEach( this::prepareSystemProperty );
		}
	}

	private void prepareSystemProperty( final SystemProperty prop ) {
		if ( prop.value().equals( SystemProperty.CLEAR ) ) {
			System.clearProperty( prop.key() );
		} else {
			System.setProperty( prop.key(), prop.value() );
		}
	}

	@Override
	public void afterEach( final ExtensionContext context ) throws Exception {
		final Store store = context.getStore( Namespace.create( getClass(), context.getRequiredTestMethod() ) );
		final Properties backup = store.get( BACKUP_STORE_KEY, Properties.class );
		System.setProperties( backup );
	}

}
