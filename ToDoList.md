# Introduction #
A list of issues in the code that need to be addressed.

## Next Priorities ##
  1. Error handling

## Comments ##
Handle comments as part of the parser rather than preprocessing with a regular expression (the regex isn't handling all conditions correctly).

## SCP ##
  1. Would like to set the rights of files uploaded via scp.  Workaround for now is to use a script execution (via the scripts declaration) to set the rights ofuploaded files


## LASIC paths as script variables ##
  1. the // syntax in lasic paths interferes with using // as comment characters in the script.  switch to \\ like scala does for XML ?
  1. Non-specific paths don't work, but should.  For example
> > `param1 : /system['sys'][0]/node['node']`
> > the node lacks any brackets ... this path should match multiple nodes
  1. Relative paths don't work, but should
> > `param1 : ../node['foo'][2]`
  1. Wildcard paths don't work, but should
> > `param1 : //node['sql master'][0]`
  1. Should lasic paths be validated before deploying anything?  That is, if a path
> > doesn't actually name anything, should we proceed with lasic verbs like deploy?
> > If such a path does not stop the deploy, what value is passed as the parameter value?

## PDSH Output ##
  1. The output of a deploy verb needs to be captured in pdsh output file

## Instance types ##
  1. Eucalyptus installations very often have different setups of machine types (more of them, different configuration) than the types on amazon.  These should work on lasic

## Volumes ##
  1. Attach an existing volume

## Compute Clusters ##

## Error Handling ##
  1. When an actor fails, needs to communicate the failure up to the Verb so the verb doesn't wait forever on a condition that will never happen

## Documentation ##
  1. Need a "getting started guide" with basic syntax and usage explanation