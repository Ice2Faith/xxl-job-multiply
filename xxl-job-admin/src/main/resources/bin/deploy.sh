#!/bin/bash

# 菜单选项
Option=$1

Param=$2

function upgrade(){
	_p_path=`pwd`
	_p_name=`basename $_p_path`
	cd ..
	_p_pkgName="${_p_name}-upgrade.tar.gz"
	echo -e "target file : \033[0;34m ../${_p_pkgName} \033[0m"
	_p_pkg=`ls -a | grep -v grep | grep "${_p_pkgName}" | head -n 1`
	echo -e "find target file : \033[0;34m ../${_p_pkg} \033[0m"
	if [ "${_p_pkg}" = "" ];
	then
		cd $_p_name
		echo -e "not found upgrade package like \033[0;34m ${_p_pkgName} \033[0m"
	else
		echo -e "upgrade by package : \033[0;34m ${_p_pkg} \033[0m ... "
		_p_now=$(date "+%Y%m%d%H%M%S")
		_p_bakDir=backup.${_p_name}
    mkdir -p ${_p_bakDir}
    _p_bakFile=${_p_bakDir}/${_p_name}.tar.gz.${_p_now}
		echo -e "backup current version to \033[0;34m ../${_p_bakFile} \033[0m ..."
		tar -czvf ${_p_bakFile} ${_p_name}  --exclude=logs --exclude=*.log > /dev/null
		echo -e "release package \033[0;34m ../${_p_pkg} \033[0m ..."
		tar -xzvf $_p_pkg  > /dev/null
		echo restart application ...
		cd $_p_name
		./jarctrl.sh restart
		echo upgrade done.
	fi
}

function cover(){
	_p_path=`pwd`
	_p_name=`basename $_p_path`
	cd ..
	_p_pkgName="${_p_name}-all.tar.gz"
	echo -e "target file : \033[0;34m ../${_p_pkgName} \033[0m"
	_p_pkg=`ls -a | grep -v grep | grep "${_p_pkgName}" | head -n 1`
	echo -e "find target file : \033[0;34m ../${_p_pkg} \033[0m"
	if [ "${_p_pkg}" = "" ];
	then
		cd $_p_name
		echo -e "not found cover package like \033[0;34m ${_p_pkgName} \033[0m"
	else
		echo -e "cover by package : \033[0;34m ${_p_pkg} \033[0m ... "
		_p_now=$(date "+%Y%m%d%H%M%S")
    _p_bakDir=backup.${_p_name}
    mkdir -p ${_p_bakDir}
    _p_bakFile=${_p_bakDir}/${_p_name}.tar.gz.${_p_now}
		echo -e "backup current version to \033[0;34m ../${_p_bakFile} \033[0m ..."
		tar -czvf ${_p_bakFile} ${_p_name}  --exclude=logs --exclude=*.log > /dev/null
		echo -e "release package \033[0;34m ../${_p_pkg} \033[0m ..."
		mv $_p_name/jarctrl.sh ./$_p_name.jarctrl.sh
		tar -xzvf $_p_pkg  > /dev/null
		mv -f ./$_p_name.jarctrl.sh $_p_name/jarctrl.sh
		echo restart application ...
		cd $_p_name
		./jarctrl.sh restart
		echo cover done.
	fi
}

function pick(){
	_p_usePath=$Param
	_p_useName=`basename ${_p_usePath}`
	_p_useNewName=${_p_useName%.*}
	echo -e "pick file \033[0;34m ${_p_usePath} \033[0m real name is \033[0;34m ${_p_useNewName} \033[0m ..."

	_p_path=`pwd`
	_p_name=`basename $_p_path`
	_p_pkgName="${_p_name}.tar.gz"

	echo -e "pick file require real name is \033[0;34m ${_p_pkgName} \033[0m ..."

  _p_usePath=`realpath ${_p_usePath}`
	cd ..

	_p_eqUseName=`basename ${_p_useNewName} .tar.gz`
	_p_eqPkgName=`basename ${_p_pkgName} .tar.gz`

	if [[ "${_p_eqUseName}" -ne "${_p_eqPkgName}" ]]; then
		cd $_p_name
		echo -e "pick package not like \033[0;34m ${_p_pkgName} \033[0m"
	else
		_p_now=$(date "+%Y%m%d%H%M%S")
		_p_bakDir=backup.${_p_name}
    mkdir -p ${_p_bakDir}
    _p_bakFile=${_p_bakDir}/${_p_name}.tar.gz.${_p_now}
		echo -e "backup current version to \033[0;34m ../${_p_bakFile} \033[0m ..."
		tar -czvf ${_p_bakFile} ${_p_name} --exclude=logs --exclude=*.log > /dev/null

		echo -e "copy pick file \033[0;34m ${_p_usePath} \033[0m to \033[0;34m ../${_p_useNewName} \033[0m ..."
		cp ${_p_usePath} ${_p_useNewName}
		echo -e "release package \033[0;34m ../${_p_useNewName} \033[0m ..."
		tar -xzvf ${_p_useNewName} > /dev/null

		echo -e "remove tmp file \033[0;34m ../${_p_useNewName} \033[0m ..."
		rm -rf ./${_p_useNewName}

		echo restart application ...
		cd $_p_name
		./jarctrl.sh restart
		echo pick done.
	fi

}

# 打印帮助信息
function help()
{
    echo -e "\033[0;31m please input 1st arg:Option \033[0m"
    echo -e "    options: \033[0;34m {upgrade|cover|pick} \033[0m"
    echo -e "    params: \033[0;34m pick option has one param \033[0m"
    echo -e "\033[0;34m upgrade \033[0m : use ../{currentDirName}-upgrade.tar.gz upgrade current application and restart"
    echo -e "\033[0;34m cover   \033[0m : use ../{currentDirName}-all.tar.gz cover current application and restart"
    echo -e "\033[0;34m pick    \033[0m : use an file name like {currentDirName}.tar.gz.[^.]+ to cover current application ans restart"

    exit 1
}

# ##################################################################################################################
# 函数分配区
# ##################################################################################################################

case $Option in
    upgrade)
    upgrade;;
    cover)
    cover;;
    pick)
    pick;;
    *)
    help;;
esac
