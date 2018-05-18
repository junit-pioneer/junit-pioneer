/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit$pioneer.jupiter;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * {@code TempDirectory} is a JUnit Jupiter extension to create and clean up a
 * temporary directory.
 *
 * <p>The temporary directory is only created if a test or lifecycle method or
 * test class constructor has a parameter annotated with
 * {@link TempDir @TempDir}. If the parameter type is not {@link Path} or if the
 * temporary directory could not be created, this extension will throw a
 * {@link ParameterResolutionException}.
 *
 * <p>The scope of the temporary directory depends on where the first
 * {@link TempDir @TempDir} annotation is encountered when executing a test
 * class. The temporary directory will be shared by all tests in a class when
 * the annotation is present on a parameter of a
 * {@link org.junit.jupiter.api.BeforeAll @BeforeAll} method or the test class
 * constructor. Otherwise, e.g. when only used on test or
 * {@link org.junit.jupiter.api.BeforeEach @BeforeEach} or
 * {@link org.junit.jupiter.api.AfterEach @AfterEach} methods, each test will
 * use its own temporary directory.
 *
 * <p>When the end of the scope of a temporary directory is reached, i.e. when
 * the test method or class has finished execution, this extension will attempt
 * to recursively delete all files and directories in the temporary directory
 * and, finally, the temporary directory itself. In case deletion of a file or
 * directory fails, this extension will throw an {@link IOException} that will
 * cause the test to fail.
 *
 * <p>By default, this extension will use the default
 * {@link java.nio.file.FileSystem FileSystem} to create temporary directories
 * in the default location. However, you may instantiate this extension using
 * the {@link TempDirectory#TempDirectory(ParentDirProvider) TempDirectory(ParentDirProvider)}
 * or {@link TempDirectory#TempDirectory(Callable)} constructor and register it
 * via {@link org.junit.jupiter.api.extension.RegisterExtension @RegisterExtension}
 * to pass a custom provider to configure the parent directory for all temporary
 * directories created by this extension. This allows the use of this extension
 * with any third-party {@code FileSystem} implementation, e.g.
 * <a href="https://github.com/google/jimfs">Jimfs</a>.
 *
 * @since 0.1
 * @see TempDir
 * @see ParentDirProvider
 * @see Files#createTempDirectory
 */
public class TempDirectory implements ParameterResolver {

	/**
	 * {@code TempDir} can be used to annotate a test or lifecycle method or
	 * test class constructor parameter of type {@link Path} that should be
	 * resolved into a temporary directory.
	 *
	 * @see TempDirectory
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface TempDir {
	}

	/**
	 * {@code ParentDirProvider} can be used to configure a custom parent
	 * directory for all temporary directories created by the
	 * {@link TempDirectory} extension this is used with.
	 *
	 * @see org.junit.jupiter.api.extension.RegisterExtension
	 * @see TempDirectory#TempDirectory(ParentDirProvider)
	 */
	@FunctionalInterface
	public interface ParentDirProvider {
		/**
		 * Get the parent directory for all temporary directories created by the
		 * {@link TempDirectory} extension this is used with.
		 *
		 * @return the {@link Optional optional} parent directory for all temporary
		 * directories; must be {@link Optional#empty() empty} or contain an existing
		 * directory.
		 */
		Optional<Path> get(ParameterContext parameterContext, ExtensionContext extensionContext) throws Exception;
	}

	private static final Namespace NAMESPACE = Namespace.create(TempDirectory.class);
	private static final String KEY = "temp.dir";

	private final ParentDirProvider parentDirProvider;

	/**
	 * Create a new {@code TempDirectory} extension that uses the default
	 * {@link java.nio.file.FileSystem FileSystem} and creates temporary
	 * directories in the default location.
	 *
	 * <p>This constructor is used by the JUnit Jupiter Engine when the
	 * extension is registered via
	 * {@link org.junit.jupiter.api.extension.ExtendWith @ExtendWith}.
	 */
	public TempDirectory() {
		this((parameterContext, extensionContext) -> Optional.empty());
	}

	/**
	 * Create a new {@code TempDirectory} extension that uses the supplied
	 * {@link Callable} to configure the parent directory for the temporary
	 * directories created by this extension.
	 *
	 * <p>The {@link Optional optional} parent directory returned by the
	 * supplied {@link Callable} must be {@link Optional#empty() empty} or
	 * contain an existing directory.
	 *
	 * <p>You may use this constructor when registering this extension via
	 * {@link org.junit.jupiter.api.extension.RegisterExtension @RegisterExtension}.
	 *
	 * @param parentDirProvider used to configure the parent directory for the
	 * temporary directories created by this extension
	 */
	public TempDirectory(Callable<Optional<Path>> parentDirProvider) {
		this((parameterContext, extensionContext) -> parentDirProvider.call());
		requireNonNull(parentDirProvider);
	}

	/**
	 * Create a new {@code TempDirectory} extension that uses the supplied
	 * {@link ParentDirProvider} to configure the parent directory for the
	 * temporary directories created by this extension.
	 *
	 * <p>You may use this constructor when registering this extension via
	 * {@link org.junit.jupiter.api.extension.RegisterExtension @RegisterExtension}.
	 *
	 * @param parentDirProvider used to configure the parent directory for the
	 * temporary directories created by this extension
	 */
	public TempDirectory(ParentDirProvider parentDirProvider) {
		this.parentDirProvider = requireNonNull(parentDirProvider);
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.isAnnotated(TempDir.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter().getType();
		if (parameterType != Path.class) {
			throw new ParameterResolutionException(
				"Can only resolve parameter of type " + Path.class.getName() + " but was: " + parameterType.getName());
		}
		return extensionContext.getStore(NAMESPACE) //
				.getOrComputeIfAbsent(KEY, key -> createCloseablePath(parameterContext, extensionContext),
					CloseablePath.class) //
				.get();
	}

	private CloseablePath createCloseablePath(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Optional<Path> parentDir = getParentDirFromProvider(parameterContext, extensionContext);
		try {
			String prefix = "junit";
			// @formatter:off
			Path tempDirectory = parentDir.isPresent()
					? Files.createTempDirectory(parentDir.get(), prefix)
					: Files.createTempDirectory(prefix);
			// @formatter:on
			return new CloseablePath(tempDirectory);
		}
		catch (Exception e) {
			throw new ParameterResolutionException("Failed to create temp directory", e);
		}
	}

	private Optional<Path> getParentDirFromProvider(ParameterContext parameterContext,
			ExtensionContext extensionContext) {
		try {
			return parentDirProvider.get(parameterContext, extensionContext);
		}
		catch (Exception ex) {
			throw new ParameterResolutionException("Failed to get parent directory from provider", ex);
		}
	}

	static class CloseablePath implements CloseableResource {

		private final Path dir;

		CloseablePath(Path dir) {
			this.dir = dir;
		}

		Path get() {
			return dir;
		}

		@Override
		public void close() throws IOException {
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
					return deleteAndContinue(file);
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					return deleteAndContinue(dir);
				}

				private FileVisitResult deleteAndContinue(Path path) throws IOException {
					try {
						Files.delete(path);
					}
					catch (IOException ex) {
						throw new IOException(
							"Failed to delete temp directory " + dir.toAbsolutePath() + " at: " + path.toAbsolutePath(),
							ex);
					}
					return CONTINUE;
				}
			});
		}
	}
}
