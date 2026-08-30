```block lang-kotlin
.getat from:{Iterable<Any>} \
      index:{Int} \
     orelse:{Dynamic = DynamicValue(NOT_FOUND)}
-> Any
```

#### Return

element at the given index, or [none](../../quarkdown-stdlib/com.quarkdown.stdlib/index.html) if the index is out of bounds

#### Parameters

* **from**

  collection to get the element from
* **index**

  index of the element to get **(starting at 1)**
* **orelse**

  value to return if the index is out of bounds. If unset, `false` is returned.

#### Chaining

This function is designed to be [chained](https://quarkdown.com/wiki/syntax-of-a-function-call#chaining-calls) with other function calls:  

```block lang-kotlin
Iterable<Any>::getat index:{Int} \
     orelse:{Dynamic}
-> Any
```

