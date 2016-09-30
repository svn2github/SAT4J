function neg(x) {
    return -x;
}

function abs(x) {
    if (x > 0) {
        return x;
    }
    return -x;
}

function add(x, y) {
    return x + y;
}

function sub(x, y) {
    return x - y;
}

function mul(x, y) {
    return x * y;
}

function div(x, y) {
    return Math.floor(x / y);
}

function mod(x, y) {
    return x % y;
}

function pow(x, y) {
    t = x;
    for (i = 1; i < y; i++) {
        t = t * x;
    }
    return t;
}

function min() {
  var ret = arguments[0];
  for(var i=1; i<arguments.length; ++i) {
    if(arguments[i] < ret) {
      ret = arguments[i];
    }
  }
  return ret;
}

function max() {
  var ret = arguments[0];
  for(var i=1; i<arguments.length; ++i) {
    if(arguments[i] > ret) {
      ret = arguments[i];
    }
  }
  return ret;
}

function not(x) {
    return !x;
}

function and() {
	for(var i=0; i<arguments.length; ++i) {
		if(!arguments[i]) {
			return false;
		}
	}
	return true;
}

function or() {
	for(var i=0; i<arguments.length; ++i) {
		if(arguments[i]) {
			return true;
		}
	}
	return false;
}

function xor(x, y) {
    return x && !y || !x && y;
}

function eq() {
	var len = arguments.length-1;
	for(var i=0; i<len; ++i) {
		if(arguments[i] !== arguments[i+1]) {
			return false;
		}
	}
	return true;
}

function ne(x, y) {
    return x !== y;
}

function lt(x, y) {
    return x < y;
}

function le(x, y) {
    return x <= y;
}

function gt(x, y) {
    return x > y;
}

function ge(x, y) {
    return x >= y;
}

function ite(x, y, z) {
    if (x) {
        return y;
    }
    return z;
}

function iff(x, y) {
    return x && y || !x && !y;
}

function ifThen(x, y) {
	return (!x) || y;
}

function dist(x, y) {
	return abs(x - y);
}

function set() {
	var result = [];
	var len = arguments.length;
	for(var i=0; i<len; ++i) {
		result = result + arguments[i];
	}
	return result
}

function inSet() {
	for(var i=1; i<arguments.length; ++i) {
		if(arguments[i] == arguments[0]) return true;
	}
	return false;
}

function distinct(list, except) {
  var effectiveLength = list.length;
  if (except != null) {
    for (var j = 0; j < except.length; ++j) {
      for (var i = 0; i < effectiveLength; ++i) {
        if (list[i] === except[j]) {
          list[i] = list[effectiveLength - 1];
          --i;
          --effectiveLength
        }
      }
    }
  }
  for (var i = 0; i < effectiveLength - 1; ++i) {
    for (var j = i + 1; j < effectiveLength; ++j) {
      if (list[i] === list[j]) {
        list[j] = list[effectiveLength - 1];
        --j;
        --effectiveLength;
      }
    }
  }
  return effectiveLength;
}
