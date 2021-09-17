string: ${string} 
boolTrue: ${boolTrue?c}
boolFalse: ${boolFalse?string('yes','no')}
integer: ${integer}
floatval: ${floatval}
nil: ${nil!"Yep, it's null"}
list:
[#list list as item]
 * ${item}
[/#list]
set:
[#list set as item]
 * ${item}
[/#list]
enumVal: ${enumVal}
array:
[#list array as item]
 * ${item}
[/#list]