# Disable Default Login
Mendix module to disable the default login functionality in Mendix. If enabled, for each valid login attempt a custom microflow will be executed to determine whether the user is allowed to login. The default implementation prevents all users other than MxAdmin to login, but since this logic is done inside a microflow, this can be changed to reflect the requirements of your project.

## How to use:  
  
1) Download the module from the Mendix App Store
2) Add the ASt_DisableDefaultLogin microflow to your after startup microflow
3) Review the constants in the configuration folder inside the module
4) Make sure to the the constant 'Enabled' to true
5) Edit the AllowUserToLogin microflow to change the logic which determines whether a user is allowed to login
