<html>
    <head>
    	<#if refreshPage>
    	<meta http-equiv="refresh" content="10" />
    	</#if>
        <title>Monitor</title>
        <link href="/bootstrap-3.3.7-dist/css/bootstrap.css" rel="stylesheet" />
    </head>
    <body>
  <table class="table table-condensed">
    <thead>
      <tr>
      	<th>Status</th>
        <th>Address</th>
        <th>Port</th>
        <th>Start Time</th>
        <th>End Time</th>
        <th>Elapsed Time</th>
        <th># Processed</th>
        <th>Avg process time</th>
      </tr>
    </thead>
    <tbody>
    	<tr>
    		<td colspan="8"><b>Active</b></td>
    	</tr>
<#assign totalActive = 0>
<#assign totalInactive = 0>
<#assign totalExecuted = 0>
<#list monitorMap?values as clientDetail>
	<#if clientDetail.activeStatus>
	<#assign totalExecuted = totalExecuted + clientDetail.totalExecutions>
	<#assign totalActive = totalActive + 1>
      <tr>
      	<td>${clientDetail.activeStatus?c}</td>
        <td>${clientDetail.inetAddres}</td>
        <td>${clientDetail.inetPort}</td>
        <td>${clientDetail.startTimeString}</td>
        <td>${clientDetail.stopTimeString}</td>
        <td>${clientDetail.totalElapsedTime}</td>
        <td>${clientDetail.totalExecutions}</td>
        <td>${clientDetail.avgExecutionTime}</td>
      </tr>
	</#if>
</#list>
		<tr>
    		<td>${totalActive}</td>
    		<td colspan="5">&nbsp;</td>
    		<td colspan="2"><b>${totalExecuted}</b></td>
    	</tr>
    	<tr>
    		<td colspan="8"><b>Inactive</b></td>
    	</tr>
<#assign totalExecuted = 0>
<#list monitorMap?values as clientDetail>
	<#if !clientDetail.activeStatus>
	<#assign totalExecuted = totalExecuted + clientDetail.totalExecutions>
	<#assign totalInactive = totalInactive + 1>
      <tr>
      	<td>${clientDetail.activeStatus?c}</td>
        <td>${clientDetail.inetAddres}</td>
        <td>${clientDetail.inetPort}</td>
        <td>${clientDetail.startTimeString}</td>
        <td>${clientDetail.stopTimeString}</td>
        <td>${clientDetail.totalElapsedTime}</td>
        <td>${clientDetail.totalExecutions}</td>
        <td>${clientDetail.avgExecutionTime}</td>
      </tr>
	</#if>
</#list> 
		<tr>
    		<td>${totalInactive}</td>
    		<td colspan="5">&nbsp;</td>
    		<td colspan="2"><b>${totalExecuted}</b></td>
    	</tr>
    </tbody>
  </table>
</body>
</html>