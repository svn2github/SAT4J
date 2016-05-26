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

function min(x, y) {
    if (x < y) {
        return x;
    }
    return y;
}

function max(x, y) {
    if (x < y) {
        return y;
    }
    return x;
}

function not(x) {
    return !x;
}

function and(x, y) {
    return x && y;
}

function or(x, y) {
    return x || y;
}

function xor(x, y) {
    return x && !y || !x && y;
}

function eq(x, y) {
    return x === y;
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

function dist(x, y) {
	return abs(x - y);
}
