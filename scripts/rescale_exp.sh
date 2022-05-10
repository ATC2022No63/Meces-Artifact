source ~/scripts/variables.sh

${SCRIPT_DIR}/close_environment.sh
echo ""
echo -e "\033[31m *********************    Running Meces Experiments   ********************* \033[0m" 
${SCRIPT_DIR}/meces_rescale_exp.sh
echo ""
echo -e "\033[31m *********************    Running Order Experiments   ********************* \033[0m" 
${SCRIPT_DIR}/order_rescale_exp.sh
echo ""
echo -e "\033[31m *********************    Running Restart Experiments   ********************* \033[0m" 
${SCRIPT_DIR}/restart_rescale_exp.sh
