#!/usr/bin/env python3
"""Check report health after PIT enforces its line-coverage and mutation thresholds."""

import sys
from collections import Counter
from xml.etree import ElementTree

report = ElementTree.parse(sys.argv[1])
counts = Counter(mutation.attrib["status"] for mutation in report.findall("mutation"))
print(dict(counts))
healthy = {"KILLED", "SURVIVED", "NO_COVERAGE"}
if not counts or set(counts) - healthy:
    sys.exit("Relatório vazio ou análise de mutação com erros/timeouts.")
