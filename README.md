# NexelArena

**NexelArena** is an **INSANELY fast**, fully **asynchronous arena regenerator**.

> ⚠ **PaperMC ONLY** — This will **NOT** work on Spigot or non-Paper forks!

---

## Features

- **Blazing Fast Performance**:
    - 15 million blocks in around **400 milliseconds**.

- **Zero Dependencies**:
    - No WorldEdit.
    - No FAWE.
    - Just drop it in.

- **Async Pasting**:
    - **Fully** asynchronous schematic pasting.
    - No lag, even during large arena resets.

---

## Installation

1. Compile the NexelArena plugin.
2. Drop it into your server's `/plugins/` folder.
3. Restart your server (you can reload as well, though not really recommended).

---

## Benchmarks

> Below is **NexelArena** setting **300x150x300** (\~14 million blocks):
>
![Benchmark 300x150x300](images/block_benchmark.png)

> Below is **NexelArena** setting **500x200x500** (\~50 million blocks):
>
![Benchmark 500x200x500](images/block_benchmark_2.png)

> Below is **NexelArena** saving a schematic of 50 million blocks, loading it, pasting it, loading
> it again (from cache), and then pasting it once more:
>
![Schematic Benchmark 50 Million Blocks](images/schematic_benchmark_1.png)

> **Note**: While this may look like it takes "a lot" of time, the reality is that it **does NOT** lag your server at
> all. Even when setting **50 million blocks**, the MSPT increase is only around 0.3 (since it runs
> asynchronously).
>
> Benchmarks were performed on a **Ryzen 7 3700X**, using **[Leaf](https://github.com/Winds-Studio/Leaf) 1.21.4**,
> actual results may vary based on your hardware.
---

## Usage

### Configuration

```hocon
version = "${version}"
```

⚠ **Do not change this.**
This is used internally by NexelArena to track config versioning and updates.

---

#### general

```hocon
general {
  enable_log_filter = true
  enable_schematic_cache = true
}
```

* `enable_log_filter`:
  Hides noisy block entity log spam caused by middle level operations.
  Recommended if you don't want useless warnings in your console.

* `enable_schematic_cache`:
  Caches schematics in memory for **instant** load speeds.
  Enabled by default, uses more memory (10M blocks ≈ 300 MB), but it’s worth it.

---

#### performance

```hocon
performance {
  add_chunks_to_force_load = true
  refresh_chunks_async = false
}
```

* `add_chunks_to_force_load`:
  Forces chunks to stay loaded after pasting, prevents un/loading all chunks.
  Highly recommended.

* `refresh_chunks_async`:
  Refreshes chunks asynchronously after pasting.
  Highly recommended if possible.

---

#### testing

```hocon
testing {
  enable_benchmark_command = false
  enable_benchmark_blocks_command = false
}
```

* `enable_benchmark_command`:
  Enables `/benchmarkschematics` to test save/load/paste times.
  WIP — not fully finished.

* `enable_benchmark_blocks_command`:
  Enables `/benchmarkblocks` to stress test block setting speeds.

---

Command usage is coming soon!

---

## To Do

- Add automatic arena regeneration.
- Create an API for developers.
- Create an actual arena system (rather files based).
- More features!