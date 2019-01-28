package com.nu.art.modular.tests;

import com.nu.art.belog.Logger;
import com.nu.art.core.generics.Processor;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BaseTest
	extends Logger {

	public class AsyncTestim<T> {

		private String name;
		private ArrayList<AsyncTest<T>> tests = new ArrayList<>();

		public AsyncTestim<T> addTest(AsyncTest<T> test) {
			tests.add(test);
			return this;
		}

		public final AsyncTestim<T> setName(String name) {
			this.name = name;
			return this;
		}

		public void execute() {
			final AtomicInteger counter = new AtomicInteger();
			for (final AsyncTest<T> test : tests) {
				counter.incrementAndGet();
				new Thread(new Runnable() {
					@Override
					public void run() {
						test.execute();
						counter.decrementAndGet();
						synchronized (counter) {
							counter.notify();
						}
					}
				}, "test--" + test.name).start();
			}

			while (counter.get() > 0) {
				try {
					synchronized (counter) {
						counter.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			AsyncTest<T>[] failedTests = getFailedTests();
			if (failedTests.length == 0)
				return;

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			logError("Error in test " + name);
			for (AsyncTest<?> failedTest : failedTests) {
				logError(" - Action name: " + failedTest.getName());
				logError(failedTest.getException());
				logError("            ----------------------------------------------------             ");
			}

			throw new AsyncTestException();
		}

		@SuppressWarnings( {
			                   "unchecked",
			                   "SuspiciousToArrayCall"
		                   })
		private AsyncTest<T>[] getFailedTests() {
			ArrayList<AsyncTest<T>> failedTests = new ArrayList<>();
			for (final AsyncTest<T> test : tests) {
				if (!test.validate())
					failedTests.add(test);
			}
			return (AsyncTest<T>[]) failedTests.toArray(new AsyncTest[0]);
		}

		public String getName() {
			return name;
		}
	}

	public interface TestValidator<ResultType> {

		boolean validate(ResultType result, Throwable t);
	}

	protected final class AsyncTest<T> {

		private final AtomicReference<T> ref = new AtomicReference<>();
		private String name;
		private String description;
		private Processor<AsyncTest<T>> processor;
		private TestValidator<T> validator;
		private T expectedValue;
		private Throwable t;
		private int timeout = 10000;

		public AsyncTest() { }

		public String getName() {
			return name;
		}

		public AsyncTest<T> setName(String name) {
			this.name = name;
			return this;
		}

		public AsyncTest<T> setDescription(String description) {
			this.description = description;
			return this;
		}

		public AsyncTest<T> setTimeout(int timeout) {
			this.timeout = timeout;
			return this;
		}

		public AsyncTest<T> setProcessor(Processor<AsyncTest<T>> processor) {
			this.processor = processor;
			return this;
		}

		public AsyncTest<T> setValidator(TestValidator<T> validator) {
			this.validator = validator;
			return this;
		}

		public AsyncTest<T> setValidator(final boolean expectedSuccess) {
			this.validator = new TestValidator<T>() {
				@Override
				public boolean validate(T result, Throwable t) {
					return expectedSuccess == (t == null);
				}
			};
			return this;
		}

		private synchronized void _notify() {
			this.notify();
		}

		public final synchronized void _set(T value) {
			logDebug("Setting result: " + value);

			ref.set(value);
			_notify();
		}

		private synchronized void _wait(int timeout) {
			logInfo("Waiting: " + timeout + "ms");

			try {
				this.wait(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private synchronized void _wait() {
			logInfo("Waiting...");

			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public final synchronized void _set(Throwable t) {
			logError("Setting error: ", t);

			this.t = t;
			_notify();
		}

		public Throwable getException() {
			return t;
		}

		private boolean validate() {
			T result = ref.get();
			if (t == null)
				if (result == null)
					this.t = new RuntimeException("Did not receive result");
				else if (!result.equals(expectedValue))
					this.t = new RuntimeException("Did not receive expected value:\n  Expected: " + expectedValue + "\n  Found: " + result);

			return validator.validate(result, this.t);
		}

		private void execute() {
			logInfo("Running  test: " + description);
			processor.process(this);
			_wait(timeout);
		}

		public AsyncTest<T> expectedValue(T expectedValue) {
			this.expectedValue = expectedValue;
			return this;
		}
	}

	protected final <T> AsyncTestim<T> createTestGroup() {
		return createTestGroup(Thread.currentThread().getStackTrace()[2].getMethodName());
	}

	protected final AsyncTest<Boolean> createDefaultTest(String name, String description) {
		return new AsyncTest<Boolean>()
			.setName(name)
			.setDescription(description)
			.expectedValue(true)
			.setValidator(true);
	}

	protected final <T> AsyncTestim<T> createTestGroup(String name) {
		return new AsyncTestim<T>().setName(name);
	}
}
