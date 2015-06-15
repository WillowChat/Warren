package engineer.carrot.warren.warren;

import java.util.Set;

public interface IPrefixListener {
    void prefixesChanged(Set<String> prefixes);
}
