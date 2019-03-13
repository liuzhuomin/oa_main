package com.yihongyu.springyhy.webapp;

import java.util.Map;

public interface ServiceExporter {

	Map<String, Class<?>[]> getExportServices();

}
