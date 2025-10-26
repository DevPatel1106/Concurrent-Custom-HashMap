# ConcurrentCustomMap

A custom thread-safe HashMap implementation in Java with modular bucket designs, including lock-free, tree-based, and dynamically resizable buckets. Fully tested with unit tests and performance benchmarks, providing insights into multi-threaded map performance.

---
## 🧠 Description

- Designed and implemented a thread-safe custom HashMap in Java supporting bucket-level locking, lock-free buckets using AtomicReference, dynamic resizing, and tree-based buckets for handling long chains.
- Engineered and executed multi-threaded benchmarks with 8 threads performing 100,000+ operations each, comparing against ConcurrentHashMap and Collections.synchronizedMap.
- Measured and optimized performance metrics including throughput (~4.6M–430K ops/sec), average latency (100–3,000 ns/op), memory usage, and bucket load distribution to validate thread-safety and efficiency.
- Applied modern concurrency techniques such as ExecutorService, AtomicLong, and fine-grained locking, ensuring zero data corruption under high-concurrency workloads.
- Structured and modularized code using Factory Design Pattern to instantiate different bucket types (standard, lock-free, tree-based) and Entry objects, enabling seamless integration of advanced features.

---

## 🔹 Features

- Thread-safe operations (put, get, remove) with bucket-level locking.
- Multiple bucket implementations:
- Bucket: basic lock-based bucket
- LockFreeBucket: atomic-based lock-free bucket
- TreeBucket: balanced tree for long hash chains
- ResizableConcurrentMap: dynamically increases buckets to reduce collisions
- Unit tests with JUnit
- Performance benchmarking with multi-threaded simulation
- Optional bucket load visualization in console

---
## 📂 Project Structure

```pgsql
ConcurrentCustomMap/
├─ src/
│  ├─ main/java/
|  |  ├─concurrentmap/
│  │  │  ├─ Bucket.java
│  │  │  ├─ BucketInterface.java
│  │  │  ├─ LockFreeBucket.java
│  │  │  ├─ TreeBucket.java
│  │  │  ├─ ConcurrentCustomMap.java
│  │  │  ├─ ResizableConcurrentMapp.java
│  │  │  └─ Entry.java
│  │  └─utils/
│  │     └─ HashUtils.java
│  └─ test/java/concurrentmap/
│     ├─ ConcurrentCustomMapTest.java
│     └─ BenchmarkTest.java 
├─ pom.xml
└─ README.md
```
---

## 🧪 Unit Testing

File: ConcurrentCustomMapTest.java
- Validates correctness in single-threaded and multi-threaded operations
- Ensures thread safety under concurrency

---

## 🚀 Benchmark Results

**Test Parameters:** 8 threads, 100,000 operations per thread, 80% reads / 20% writes

| Map Type                  | Total Time (ns)  | Ops/sec      | Avg Latency (ns/op) | Total Ops | Memory Change (bytes) |
|----------------------------|-----------------|-------------|--------------------|-----------|----------------------|
| **Bucket**                 | 1,858,227,900   | 430,517     | 2,323              | 800,000   | 7,608,920            |
| **LockFreeBucket**         | 2,444,193,400   | 327,306     | 3,055              | 800,000   | 29,009,128           |
| **TreeBucket**             | 170,925,300     | 4,680,407   | 214                | 800,000   | 10,975,920           |
| **ResizableConcurrentMap** | 2,176,200,400   | 367,613     | 2,720              | 800,000   | 8,816,984            |
| **ConcurrentHashMap**      | 80,673,500      | 9,916,515   | 101                | 800,000   | 13,412,696           |
| **SynchronizedMap**        | 133,428,500     | 5,995,720   | 167                | 800,000   | 10,102,928           |

---

## 🔹 Bucket Load Distribution (Example)

Bucket
```
[00] ################################################# (4948)
[01] ################################################# (4997)
[02] ################################################# (4976)
...
[15] ################################################# (4969)
```

LockFreeBucket
```
[00] ################################################# (4978)
[01] ################################################# (4988)
...
[15] ################################################# (4976)
```

TreeBucket
```
[00] ################################################ (4904)
[01] ################################################## (5054)
...
[15] ################################################# (4977)
```

ResizableConcurrentMap
```
[00] ################################################# (4942)
[01] ################################################# (4955)
...
[15] ################################################# (5025)
```

---

## 🔹 Observations & Inference

TreeBucket performance:
- Provides excellent latency and throughput for high-collision scenarios.
- Ops/sec (~4.68M) far outperforms lock-based or lock-free buckets due to balanced tree traversal and minimized contention.

LockFreeBucket:
- Higher memory usage (~29MB) due to atomic operations and retries.
- Slower than basic Bucket (~327K vs 430K ops/sec) under heavy operations.

ResizableConcurrentMap:
- Reduces bucket collisions dynamically.
- Slightly better than basic Bucket in memory distribution and ops/sec, but not as fast as TreeBucket.

Comparison with standard Java maps:
- ConcurrentHashMap is fastest in throughput (~9.9M ops/sec) due to highly optimized internal concurrency.
- SynchronizedMap has moderate performance (~5.99M ops/sec) but simpler design.
- Custom maps are excellent for learning, benchmarking, and experimenting with lock-free and tree-based bucket strategies.
- Bucket load distribution is roughly uniform across all implementations, indicating good hash function performance.

---

## 🏃‍♂️ Example Usage
```
ConcurrentCustomMap<String, Integer> map = new ConcurrentCustomMap<>();
map.put("one", 1);
map.put("two", 2);
System.out.println(map.get("one")); // 1
map.remove("two");
```
---

## ✨ Run benchmark:
```
mvn test -Dtest=BenchmarkTest
```
---
