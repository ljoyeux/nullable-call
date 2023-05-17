<h1>NullableCall</h1>

NullableCall provides a mechanism to perform calls on null objects. 

By default, on languages such as ObjectiveC, a call on a null object returns null value.

The same basic behavior is implemented.

Primary usage of NullableCall is when navigating in objects for reading values, as results of a web service call.

## Usage

Nullable proxy is by calling NullableCall.nullableCall where a non object is null is passed or a type and a nullable object is passed.
```java
T proxy = NullableCall.nullableCall(T object)
T proxy = NullableCall.nullableCall(Class<T> c, T object)
```

Examples are available in tests.
