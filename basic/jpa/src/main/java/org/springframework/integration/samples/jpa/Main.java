 already addresses the issue of conditionally invoking methods. The `Runtime.getRuntime().addShutdownHook(new Thread(context::close));` line is only executed if `context` is not null and the context is active. This ensures that the `close()` method is only called on a valid and running application context.

Therefore, no changes are neede